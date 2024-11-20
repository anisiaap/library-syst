package com.example.bureaucratic_system_backend.service;

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

    // Generate a fee for overdue returns
    public void generateOverdueFee(String borrowId, String membershipId, String dueDate, String actualReturnDate) {
        feeLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = feeLocks.get(borrowId);

        lock.lock();
        try {
            LocalDate due = LocalDate.parse(dueDate);
            LocalDate returned = LocalDate.parse(actualReturnDate);

            long overdueDays = ChronoUnit.DAYS.between(due, returned);
            if (overdueDays > 0) {
                String amount = String.valueOf(overdueDays * 1); // Example: $1 per day
                Fees fee = new Fees(borrowId, membershipId, amount, borrowId, "false", dueDate);
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

    // Calculate and update overdue fees for a given borrow ID
    public void updateOverdueFee(String borrowId, String actualReturnDate) {
        feeLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = feeLocks.get(borrowId);

        lock.lock();
        try {
            Fees fee = firebaseService.getFeeByBorrowId(borrowId);
            if (fee != null && fee.getDueDate() != null) {
                LocalDate dueDate = LocalDate.parse(fee.getDueDate());
                LocalDate returnDate = LocalDate.parse(actualReturnDate);

                long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                if (overdueDays > 0) {
                    String newAmount = String.valueOf(overdueDays * 1); // Example: $1 per day
                    fee.setAmount(newAmount);
                    firebaseService.updateFee(borrowId, fee);
                    logger.info("Overdue fee updated: $ {} for borrow ID: {}", newAmount, borrowId);
                } else {
                    logger.info("No update needed. Book returned on time for borrow ID: {}", borrowId);
                }
            } else {
                logger.warn("Fee or due date not found for borrow ID: {}", borrowId);
            }
        } catch (Exception e) {
            logger.error("Error updating overdue fee for borrow ID: {}: {}", borrowId, e.getMessage());
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