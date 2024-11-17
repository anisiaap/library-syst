package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Department;
import com.example.bureaucratic_system_backend.model.LoanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BookLoaningService implements Department {

    private static final Logger logger = LoggerFactory.getLogger(BookLoaningService.class);

    //private final FirebaseService firebaseService;

    private final Queue<LoanRequest> queue = new LinkedBlockingQueue<>();
    private final Map<String, Lock> bookLocks = new ConcurrentHashMap<>();
    private final Thread counter1;
    private static BookLoaningService instance;
    private final Thread counter2;
    private volatile boolean counter1Paused = false;
    private volatile boolean counter2Paused = false;


    public BookLoaningService() {


        counter1 = new Thread(() -> processQueue(1));
        counter2 = new Thread(() -> processQueue(2));
        counter1.start();
        counter2.start();

        logger.info("BookLoaningService initialized. Counters started.");
    }
    public static synchronized BookLoaningService getInstance() {

        if (instance == null) {
            instance = new BookLoaningService();
        }
        return instance;
    }

    public void addCitizenToQueue(Citizen citizen, String bookTitle, String bookAuthor) {
        synchronized (queue) {
            queue.add(new LoanRequest(bookTitle,bookAuthor,citizen.getId()));
            queue.notifyAll();
            logger.info("Added citizen with ID {} to the queue for book '{}' by '{}'.", citizen.getId(), bookTitle, bookAuthor);
        }
    }

    private void processQueue(int counterId) {
        while (true) {
            try {
                final Object pauseLock = (counterId == 1) ? counter1 : counter2;
                synchronized (pauseLock) {
                    // Wait if the specific counter is paused
                    while ((counterId == 1 && counter1Paused) || (counterId == 2 && counter2Paused)) {
                        System.out.println("Counter " + counterId + " is paused, waiting...");
                        pauseLock.wait();
                    }
                }

                // Check for the global pause condition when both counters are paused
                synchronized (this) {
                    // If both counters are paused, wait on the global object
                    if (counter1Paused && counter2Paused) {
                        System.out.println("Both counters are paused, waiting...");
                        this.wait();
                        continue; // After being notified, check the pause condition again
                    }
                }

                LoanRequest request = null;
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        request = queue.poll();
                    }
                }

                // If a request is available, process it
                if (request != null) {
                    tryToBorrowBook(request.getCitizenId(), request.getBookTitle(), request.getBookAuthor());
                } else {
                    // If the queue is empty, wait for new requests
                    synchronized (queue) {
                        System.out.println("Queue is empty, waiting for requests...");
                        queue.wait();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interruption status
                System.out.println("Thread interrupted: " + counterId);
                return; // Optionally return to end the thread or handle interruption appropriately
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
                FirebaseService.updateBook(book);
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
        if (counterId == 1) {
            counter1Paused = true;
        } else if (counterId == 2) {
            counter2Paused = true;
        }
        logger.info("Paused counter {}.", counterId);
    }

    @Override
    public void resumeCounter(int counterId) {
        if (counterId == 1) {
            counter1Paused = false;
            synchronized (counter1) {
                counter1.notify();
            }
        } else if (counterId == 2) {
            counter2Paused = false;
            synchronized (counter2) {
                counter2.notify();
            }
        }
        logger.info("Resumed counter {}.", counterId);
    }
}
