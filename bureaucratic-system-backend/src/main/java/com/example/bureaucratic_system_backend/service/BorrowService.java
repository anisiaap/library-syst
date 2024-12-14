package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Borrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);

    private final Map<String, Lock> borrowLocks = new ConcurrentHashMap<>();
    private final FirebaseService firebaseService;

    public BorrowService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    // ----------------------- Borrow Management -----------------------

    public void createBorrow(String borrowId, String bookId, String membershipId) {
        borrowLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = borrowLocks.get(borrowId);

        lock.lock();
        try {
            String borrowDate = LocalDate.now().toString();
            String dueDate = LocalDate.now().plusDays(30).toString(); // Default loan period is 30 days
            Borrows borrow = new Borrows(borrowId, bookId, membershipId, borrowDate, dueDate, null);
            firebaseService.addBorrow(borrow);

            logger.info("Borrow record created successfully for borrow ID: {}", borrowId);
        } catch (Exception e) {
            logger.error("Error creating borrow record for borrow ID: {}: {}", borrowId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void updateReturnDate(String borrowId, String returnDate) {
        borrowLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = borrowLocks.get(borrowId);

        lock.lock();
        try {
            Borrows borrow = firebaseService.getBorrowById(borrowId);
            if (borrow != null) {
                borrow.setReturnDate(returnDate);
                firebaseService.updateBorrow(borrowId, borrow);
                logger.info("Return date updated successfully for borrow ID: {}", borrowId);
            } else {
                logger.warn("Borrow record not found for borrow ID: {}", borrowId);
            }
        } catch (Exception e) {
            logger.error("Error updating return date for borrow ID: {}: {}", borrowId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public Borrows getBorrowById(String borrowId) {
        try {
            return firebaseService.getBorrowById(borrowId);
        } catch (Exception e) {
            logger.error("Error retrieving borrow record for borrow ID: {}: {}", borrowId, e.getMessage());
            return null;
        }
    }

    public void deleteBorrow(String borrowId) {
        borrowLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = borrowLocks.get(borrowId);

        lock.lock();
        try {
            firebaseService.deleteBorrow(borrowId);
            logger.info("Borrow record deleted successfully for borrow ID: {}", borrowId);
        } catch (Exception e) {
            logger.error("Error deleting borrow record for borrow ID: {}: {}", borrowId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
