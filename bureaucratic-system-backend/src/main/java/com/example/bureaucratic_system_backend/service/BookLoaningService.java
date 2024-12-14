package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.*;
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
    private final List<Counter> countersList = new ArrayList<>();
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
        try (BufferedReader reader = new BufferedReader(new FileReader("/Users/anisiapirvulescu/Desktop/bureaucratic-system-2/bureaucratic-system-backend/src/main/java/com/example/bureaucratic_system_backend/config/config.txt"))) {
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
        logger.info("Initializing {} counters...", numberOfCounters);
        FirebaseService.clearCountersCollection(); // Clean Firestore counters collection

        for (int i = 1; i <= numberOfCounters; i++) {
            final int counterId = i;

            // Initialize lock for the counter
            counterLocks.put(counterId, new Object());

            // Add initial paused state for the counter
            counterPauseStatus.put(counterId, false);

            // Create and save the Counter model to Firestore
            Counter counter = new Counter(counterId, false); // Default: not paused
            countersList.add(counter);
            FirebaseService.saveCounterToFirestore(counter);

            logger.info("Counter {} initialized in memory and saved to Firestore.", counterId);
        }

        // Set up Firestore listener for counters
        FirebaseService.listenToCounterChanges(counterLocks, counterPauseStatus);

        // Start threads for each counter
        for (int i = 1; i <= numberOfCounters; i++) {
            final int counterId = i;
            Thread counterThread = new Thread(() -> processQueue(counterId));
            counters.add(counterThread);
            counterThread.start();

            logger.info("Thread for Counter {} started.", counterId);
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
        Object lock = counterLocks.get(counterId);

        if (lock == null) {
            logger.error("No lock found for counter {}. Exiting thread.", counterId);
            return; // Stop processing if no lock is found
        }

        while (true) {
            try {
                synchronized (lock) {
                    while (isCounterPausedInFirestore(counterId)) {
                        logger.info("Counter {} is paused. Waiting...", counterId);
                        lock.wait(); // Wait until notified
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
                        queue.wait(); // Wait for new requests
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Counter {} interrupted. Exiting...", counterId);
                return;
            }
        }
    }
    private boolean isCounterPausedInFirestore(int counterId) {
        try {
            Map<String, Object> counterData = FirebaseService.getCounterById(counterId);
            if (counterData != null) {
                // Check if the "isPaused" key exists and cast the value properly
                if (counterData.containsKey("isPaused")) {
                    Object pausedState = counterData.get("isPaused");
                    if (pausedState instanceof Boolean) {
                        return (boolean) pausedState;
                    } else {
                        logger.warn("Invalid data type for 'isPaused' in counter {}. Assuming paused.", counterId);
                        return true; // Assume paused if data type is invalid
                    }
                } else {
                    logger.warn("'isPaused' field not found for counter {}. Assuming paused.", counterId);
                    return true; // Assume paused if key is missing
                }
            } else {
                logger.warn("No counter data found for counter {}. Assuming paused.", counterId);
                return true; // Assume paused if no data is found
            }
        } catch (Exception e) {
            logger.error("Error fetching counter state from Firestore for counter {}: {}", counterId, e.getMessage());
            return true; // Assume paused in case of error
        }
    }

    private void tryToBorrowBook(String citizenId, String bookTitle, String bookAuthor) {
        logger.info("Attempting to borrow book '{}' by '{}' for citizen ID {}.", bookTitle, bookAuthor, citizenId);

        String membershipId = FirebaseService.getMembershipIdById(citizenId);
        if (membershipId == null) {
            logger.warn("Citizen ID {} does not have a valid membership.", citizenId);
            return;
        }

        Borrows existingBorrow = FirebaseService.getBorrowByMembershipAndBook(membershipId, bookTitle, bookAuthor);
        if (existingBorrow != null) {
            logger.warn("Citizen ID {} has already borrowed the book '{}' by '{}' and has not returned it yet.",
                    citizenId, bookTitle, bookAuthor);
            return;
        }

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
                FirebaseService.updateBookField(book.getId(), "available", false);
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
        countersList.stream()
                .filter(counter -> counter.getCounterId() == counterId)
                .findFirst()
                .ifPresent(counter -> {
                    counter.setPaused(true);
                    FirebaseService.updateCounterState(counterId, true); // Update state in Firebase
                    logger.info("Paused counter {}.", counterId);
                });
    }

    @Override
    public void resumeCounter(int counterId) {
        countersList.stream()
                .filter(counter -> counter.getCounterId() == counterId)
                .findFirst()
                .ifPresent(counter -> {
                    counter.setPaused(false);
                    FirebaseService.updateCounterState(counterId, false); // Update state in Firebase
                    synchronized (counterLocks.get(counterId)) {
                        counterLocks.get(counterId).notify();
                    }
                    logger.info("Resumed counter {}.", counterId);
                });
    }

}
