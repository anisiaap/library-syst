package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Department;
import com.example.bureaucratic_system_backend.model.LoanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BookLoaningService implements Department {

    private static final Logger logger = LoggerFactory.getLogger(BookLoaningService.class);


    private final Queue<LoanRequest> queue = new LinkedBlockingQueue<>();
    private final Map<String, Lock> bookLocks = new ConcurrentHashMap<>();
    private static BookLoaningService instance;

    private final List<Thread> counters = new ArrayList<>();
    private final BorrowService borrowService;
    private final Object globalPauseLock = new Object();
    private volatile boolean globalPause = false;
    private volatile boolean counter1Paused = false;
    private volatile boolean counter2Paused = false;

    private final Map<Integer, Boolean> counterPauseStatus = new ConcurrentHashMap<>();
    private final Map<Integer, Object> counterLocks = new ConcurrentHashMap<>();


    public BookLoaningService(BorrowService borrowService) {
        this.borrowService = borrowService;
        int numberOfCounters = readCounterConfig();
        initializeCounters(numberOfCounters);
        logger.info("BookLoaningService initialized with {} counters.", numberOfCounters);
    }

    private int readCounterConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/anisiapirvulescu/Desktop/cebp/bureaucratic-system-backend/src/main/java/com/example/bureaucratic_system_backend/config/config.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("counters=")) {
                    return Integer.parseInt(line.split("=")[1].trim());
                }
            }
        } catch (IOException | NumberFormatException e) {
            logger.error("Error reading configuration file. Defaulting to 2 counters.", e);
        }
        return 2; // Default number of counters
    }

    private void initializeCounters(int numberOfCounters) {
        for (int i = 1; i <= numberOfCounters; i++) {
            final int counterId = i;
            counterPauseStatus.put(counterId, false);
            counterLocks.put(counterId, new Object());
            Thread counterThread = new Thread(() -> processQueue(counterId));
            counters.add(counterThread);
            counterThread.start();
        }
    }

    public void addCitizenToQueue(Citizen citizen, String bookTitle, String bookAuthor) {
        synchronized (queue) {
            queue.add(new LoanRequest(bookTitle, bookAuthor, citizen.getId()));
            queue.notifyAll();
            logger.info("Added citizen with ID {} to the queue for book '{}' by '{}'.", citizen.getId(), bookTitle, bookAuthor);
        }
    }

    private void processQueue(int counterId) {
        while (true) {
            try {
                synchronized (counterLocks.get(counterId)) {
                    while (counterPauseStatus.get(counterId)) {
                        logger.info("Counter {} is paused. Waiting...", counterId);
                        counterLocks.get(counterId).wait();
                    }
                }

                LoanRequest request = null;
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        request = queue.poll();
                    }
                }

                if (request != null) {
                    tryToBorrowBook(request.getCitizenId(), request.getBookTitle(), request.getBookAuthor());
                } else {
                    synchronized (queue) {
                        queue.wait();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Counter {} interrupted. Exiting...", counterId);
                return;
            }
        }
    }

    private void tryToBorrowBook(String citizenId, String bookTitle, String bookAuthor) {
        logger.info("Attempting to borrow book '{}' by '{}' for citizen ID {}.", bookTitle, bookAuthor, citizenId);
        Book book = FirebaseService.getBookByTitleAndAuthor(bookTitle, bookAuthor);
        if (book == null) {
            logger.warn("Book '{}' by '{}' not found in the system.", bookTitle, bookAuthor);
            return;
        }

        bookLocks.putIfAbsent(book.getId(), new ReentrantLock());
        Lock bookLock = bookLocks.get(book.getId());

        bookLock.lock();
        try {
            if (book.isAvailable() && FirebaseService.getMembershipIdById(citizenId) != null) {
                logger.info("Book '{}' by '{}' is available. Assigning it to citizen ID {}.", bookTitle, bookAuthor, citizenId);
                book.setAvailable(false);
                String membershipId = FirebaseService.getMembershipIdById(citizenId);
                //FirebaseService.borrowBook(book.getId(), FirebaseService.getMembershipIdById(citizenId));
                String borrowId = UUID.randomUUID().toString();
                borrowService.createBorrow(borrowId, book.getId(), membershipId);

                logger.info("Book '{}' by '{}' successfully loaned to citizen ID {}.", bookTitle, bookAuthor, citizenId);
            } else {
                logger.warn("Book '{}' by '{}' is unavailable or citizen ID {} does not have a valid membership.", bookTitle, bookAuthor, citizenId);
            }
        } finally {
            bookLock.unlock();
            logger.info("Released lock for book '{}' by '{}'.", bookTitle, bookAuthor);
        }
    }

    @Override
    public void pauseCounter(int counterId) {
        if (counterPauseStatus.containsKey(counterId)) {
            counterPauseStatus.put(counterId, true);
            logger.info("Paused counter {}.", counterId);
        } else {
            logger.warn("Counter {} does not exist. Unable to pause.", counterId);
        }
    }

    @Override
    public void resumeCounter(int counterId) {
        if (counterPauseStatus.containsKey(counterId)) {
            counterPauseStatus.put(counterId, false);
            synchronized (counterLocks.get(counterId)) {
                counterLocks.get(counterId).notify();
            }
            logger.info("Resumed counter {}.", counterId);
        } else {
            logger.warn("Counter {} does not exist. Unable to resume.", counterId);
        }
    }
}
