package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Borrows;
import com.example.bureaucratic_system_backend.model.Fees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FeeService {

    private static final Logger logger = LoggerFactory.getLogger(FeeService.class);

    // Locks for thread-safe fee management
    private final Map<String, Lock> feeLocks = new ConcurrentHashMap<>();
    private final FirebaseService firebaseService;

    public FeeService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    // ----------------------- Fee Management -----------------------

    // Generate overdue fee based on borrow data
    public void generateOverdueFee(String borrowId) {
        feeLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = feeLocks.get(borrowId);

        lock.lock();
        try {
            Borrows borrow = firebaseService.getBorrowById(borrowId);
            if (borrow == null) {
                logger.warn("Borrow record not found for borrow ID: {}", borrowId);
                return;
            }

            if (borrow.getReturnDate() == null) {
                logger.warn("Book not returned yet for borrow ID: {}", borrowId);
                return;
            }

            LocalDate dueDate = LocalDate.parse(borrow.getDueDate());
            LocalDate returnDate = LocalDate.parse(borrow.getReturnDate());

            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            if (overdueDays > 0) {
                String amount = String.valueOf(overdueDays * 1); // Example: $1 per day
                Fees fee = new Fees(borrowId, borrow.getMembershipId(), amount, borrowId, "false");
                firebaseService.addFee(fee);
                logger.info("Overdue fee generated: $ {} for borrow ID: {}", amount, borrowId);
            } else {
                logger.info("No overdue fee generated. Book returned on time for borrow ID: {}", borrowId);
            }
        } catch (Exception e) {
            logger.error("Error generating overdue fee for borrow ID: {}: {}", borrowId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Mark a fee as paid
    public void markFeeAsPaid(String feeId) {
        feeLocks.putIfAbsent(feeId, new ReentrantLock());
        Lock lock = feeLocks.get(feeId);

        lock.lock();
        try {
            Fees fee = firebaseService.getFeeByBorrowId(feeId);
            if (fee != null) {
                fee.setPaid("true");
                firebaseService.updateFee(feeId, fee);
                logger.info("Fee marked as paid for fee ID: {}", feeId);
            } else {
                logger.warn("Fee not found for fee ID: {}", feeId);
            }
        } catch (Exception e) {
            logger.error("Error marking fee as paid for fee ID: {}: {}", feeId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Delete a fee
    public void deleteFee(String feeId) {
        feeLocks.putIfAbsent(feeId, new ReentrantLock());
        Lock lock = feeLocks.get(feeId);

        lock.lock();
        try {
            firebaseService.deleteFee(feeId);
            logger.info("Fee deleted successfully for fee ID: {}", feeId);
        } catch (Exception e) {
            logger.error("Error deleting fee for fee ID: {}: {}", feeId, e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // Retrieve a fee by borrow ID
    public Fees getFeeByBorrowId(String borrowId) {
        try {
            return firebaseService.getFeeByBorrowId(borrowId);
        } catch (Exception e) {
            logger.error("Error retrieving fee for borrow ID: {}: {}", borrowId, e.getMessage());
            return null;
        }
    }
}