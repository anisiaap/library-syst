package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Department;
import com.example.bureaucratic_system_backend.model.LoanRequest;
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

    private final FirebaseService firebaseService;

    private final Queue<LoanRequest> queue = new LinkedBlockingQueue<>();
    private final Map<String, Lock> bookLocks = new ConcurrentHashMap<>();
    private final Thread counter1;
    private final Thread counter2;
    private volatile boolean counter1Paused = false;
    private volatile boolean counter2Paused = false;

    @Autowired
    public BookLoaningService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;

        counter1 = new Thread(() -> processQueue(1));
        counter2 = new Thread(() -> processQueue(2));
        counter1.start();
        counter2.start();
    }

    public void addCitizenToQueue(Citizen citizen, String bookTitle, String bookAuthor) {
        synchronized (queue) {
            queue.add(new LoanRequest(citizen.getId(), bookTitle, bookAuthor));
            queue.notifyAll();
        }
    }

    private void processQueue(int counterId) {
        while (true) {
            try {
                synchronized (this) {
                    while ((counterId == 1 && counter1Paused) || (counterId == 2 && counter2Paused)) {
                        wait();
                    }
                }
                LoanRequest request;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        queue.wait();
                    }
                    request = queue.poll();
                }
                if (request != null) {
                    tryToBorrowBook(request.getCitizenId(), request.getBookTitle(), request.getBookAuthor());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void tryToBorrowBook(String citizenId, String bookTitle, String bookAuthor) {
        Book book = firebaseService.getBookByTitleAndAuthor(bookTitle, bookAuthor);
        if (book == null) {
            return;
        }

        bookLocks.putIfAbsent(book.getId(), new ReentrantLock());
        Lock bookLock = bookLocks.get(book.getId());

        bookLock.lock();
        try {
            if (book.isAvailable() && firebaseService.getMembershipIdById(citizenId) != null) {
                book.setAvailable(false);
                firebaseService.updateBook(book);
            } else {
                System.out.println("Book unavailable.");
            }
        } finally {
            bookLock.unlock();
        }
    }

    @Override
    public void pauseCounter(int counterId) {
        if (counterId == 1) {
            counter1Paused = true;
        } else if (counterId == 2) {
            counter2Paused = true;
        }
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
    }
}
