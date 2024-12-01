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

    // Manually add a custom fee
    public void addFee(Fees fee) {
        feeLocks.putIfAbsent(fee.getId(), new ReentrantLock());
        Lock lock = feeLocks.get(fee.getId());

        lock.lock();
        try {
            // Check if fee ID already exists (uniqueness check)
            if (firebaseService.documentExists("fees", fee.getId())) {
                throw new IllegalArgumentException("Fee with ID " + fee.getId() + " already exists.");
            }

            // Validate foreign keys (e.g., membershipId, borrowId)
            if (!firebaseService.documentExists("memberships", fee.getMembershipId())) {
                throw new IllegalArgumentException("Membership with ID " + fee.getMembershipId() + " does not exist.");
            }
            if (!firebaseService.documentExists("borrows", fee.getBorrowId())) {
                throw new IllegalArgumentException("Borrow record with ID " + fee.getBorrowId() + " does not exist.");
            }

            firebaseService.addFee(fee);
            logger.info("Fee added successfully: {}", fee);
        } catch (Exception e) {
            logger.error("Error adding fee: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    // Automatically generate overdue fee based on borrow data
    public void generateOverdueFee(String borrowId) {
        feeLocks.putIfAbsent(borrowId, new ReentrantLock());
        Lock lock = feeLocks.get(borrowId);

        lock.lock();
        try {
            // Validate that the borrow ID exists
            Borrows borrow = firebaseService.getBorrowById(borrowId);
            if (borrow == null) {
                throw new IllegalArgumentException("Borrow record not found for borrow ID: " + borrowId);
            }

            // Validate that the book has been returned
            if (borrow.getReturnDate() == null) {
                throw new IllegalArgumentException("Book not returned yet for borrow ID: " + borrowId);
            }

            // Calculate overdue fee
            LocalDate dueDate = LocalDate.parse(borrow.getDueDate());
            LocalDate returnDate = LocalDate.parse(borrow.getReturnDate());
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);

            if (overdueDays > 0) {
                String amount = String.valueOf(overdueDays * 1); // Example: $1 per day
                Fees fee = new Fees(borrowId, borrow.getMembershipId(), amount, borrowId, "false");

                // Check if fee ID already exists (reuse borrowId as fee ID)
                if (firebaseService.documentExists("fees", fee.getId())) {
                    throw new IllegalArgumentException("Overdue fee for borrow ID " + borrowId + " already exists.");
                }

                firebaseService.addFee(fee);
                logger.info("Overdue fee generated: $ {} for borrow ID: {}", amount, borrowId);
            } else {
                logger.info("No overdue fee generated. Book returned on time for borrow ID: {}", borrowId);
            }
        } catch (Exception e) {
            logger.error("Error generating overdue fee for borrow ID: {}: {}", borrowId, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
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
            // Validate that the fee exists
            Fees fee = firebaseService.getFeeById(feeId);
            if (fee == null) {
                throw new IllegalArgumentException("Fee not found for fee ID: " + feeId);
            }

            fee.setPaid("true");
            firebaseService.updateFee(feeId, fee);
            logger.info("Fee marked as paid for fee ID: {}", feeId);
        } catch (Exception e) {
            logger.error("Error marking fee as paid for fee ID: {}: {}", feeId, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
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
            // Validate that the fee exists
            if (!firebaseService.documentExists("fees", feeId)) {
                throw new IllegalArgumentException("Fee not found for fee ID: " + feeId);
            }

            firebaseService.deleteFee(feeId);
            logger.info("Fee deleted successfully for fee ID: {}", feeId);
        } catch (Exception e) {
            logger.error("Error deleting fee for fee ID: {}: {}", feeId, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    // Retrieve a fee by borrow ID
    public Fees getFeeByBorrowId(String borrowId) {
        try {
            Fees fee = firebaseService.getFeeByBorrowId(borrowId);
            if (fee == null) {
                logger.warn("No fee found for borrow ID: {}", borrowId);
            }
            return fee;
        } catch (Exception e) {
            logger.error("Error retrieving fee for borrow ID: {}: {}", borrowId, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}