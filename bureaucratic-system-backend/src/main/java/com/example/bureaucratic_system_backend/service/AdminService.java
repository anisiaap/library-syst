package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Borrows;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Membership;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    // Locks for thread-safe operations
    private final Map<String, Lock> bookLocks = new ConcurrentHashMap<>();
    private final Map<String, Lock> citizenLocks = new ConcurrentHashMap<>();
    private final Map<String, Lock> membershipLocks = new ConcurrentHashMap<>();
    private final Map<String, Lock> feeLocks = new ConcurrentHashMap<>();

    // Firebase service for database operations
    private final FirebaseService firebaseService;

    public AdminService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    // ----------------------- Books Management -----------------------


    public void addBook(Book book) {
        try {
            if (firebaseService.getDocumentById("books", book.getId()) != null) {
                throw new IllegalArgumentException("Book with ID " + book.getId() + " already exists.");
            }
            firebaseService.addBook(book);
            logger.info("Book added successfully: {}", book.getName());
        } catch (Exception e) {
            logger.error("Error adding book", e.getMessage());
            throw new RuntimeException("Error adding book: " + e.getMessage());
        }
    }

    public void updateBookField(String bookId, String fieldName, Object value) {
        if (!firebaseService.documentExists("books", bookId)) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        String lockKey = "book:" + bookId + ":" + fieldName;
        bookLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = bookLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("books", bookId, fieldName, value);
            logger.info("Book field '{}' updated successfully for book ID: {}", fieldName, bookId);
        } catch (Exception e) {
            logger.error("Error updating book field '{}' for book ID: {}", fieldName, bookId, e.getMessage());
            throw new RuntimeException("Error updating book field: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteBook(String bookId) {
        if (!firebaseService.documentExists("books", bookId)) {
            throw new IllegalArgumentException("Book with ID " + bookId + " does not exist.");
        }

        bookLocks.putIfAbsent(bookId, new ReentrantLock());
        Lock lock = bookLocks.get(bookId);

        lock.lock();
        try {
            firebaseService.deleteBook(bookId);
            logger.info("Book deleted successfully: {}", bookId);
        } catch (Exception e) {
            logger.error("Error deleting book with ID: {}", bookId, e.getMessage());
            throw new RuntimeException("Error deleting book: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ----------------------- Citizens Management -----------------------

    public void addCitizen(Citizen citizen) {
        try {
            if (firebaseService.getDocumentById("citizens", citizen.getId()) != null) {
                throw new IllegalArgumentException("Citizen with ID " + citizen.getId() + " already exists.");
            }
            firebaseService.addCitizen(citizen);
            logger.info("Citizen added successfully: {}", citizen.getName());
        } catch (Exception e) {
            logger.error("Error adding citizen", e.getMessage());
            throw new RuntimeException("Error adding citizen: " + e.getMessage());
        }
    }

    public void updateCitizenField(String citizenId, String fieldName, Object value) {
        if (!firebaseService.documentExists("citizens", citizenId)) {
            throw new IllegalArgumentException("Citizen with ID " + citizenId + " does not exist.");
        }

        String lockKey = "citizen:" + citizenId + ":" + fieldName;
        citizenLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = citizenLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("citizens", citizenId, fieldName, value);
            logger.info("Citizen field '{}' updated successfully for citizen ID: {}", fieldName, citizenId);
        } catch (Exception e) {
            logger.error("Error updating citizen field '{}' for citizen ID: {}", fieldName, citizenId, e.getMessage());
            throw new RuntimeException("Error updating citizen field: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteCitizen(String citizenId) {
        if (!firebaseService.documentExists("citizens", citizenId)) {
            throw new IllegalArgumentException("Citizen with ID " + citizenId + " does not exist.");
        }

        citizenLocks.putIfAbsent(citizenId, new ReentrantLock());
        Lock lock = citizenLocks.get(citizenId);

        lock.lock();
        try {
            firebaseService.deleteCitizen(citizenId);
            logger.info("Citizen deleted successfully: {}", citizenId);
        } catch (Exception e) {
            logger.error("Error deleting citizen with ID: {}", citizenId, e.getMessage());
            throw new RuntimeException("Error deleting citizen: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ----------------------- Memberships Management -----------------------

    public void addMembership(Membership membership) {
        try {
            if (!firebaseService.documentExists("citizens", membership.getCitizenId())) {
                throw new IllegalArgumentException("Citizen with ID " + membership.getCitizenId() + " does not exist.");
            }
            if (firebaseService.getDocumentById("memberships", membership.getMembershipNumber()) != null) {
                throw new IllegalArgumentException("Membership with number " + membership.getMembershipNumber() + " already exists.");
            }
            firebaseService.addMembership(membership);
            logger.info("Membership added successfully: {}", membership.getMembershipNumber());
        } catch (Exception e) {
            logger.error("Error adding membership", e.getMessage());
            throw new RuntimeException("Error adding membership: " + e.getMessage());
        }
    }

    public void updateMembershipField(String membershipId, String fieldName, Object value) {
        if (!firebaseService.documentExists("memberships", membershipId)) {
            throw new IllegalArgumentException("Membership with ID " + membershipId + " does not exist.");
        }

        String lockKey = "membership:" + membershipId + ":" + fieldName;
        membershipLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = membershipLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("memberships", membershipId, fieldName, value);
            logger.info("Membership field '{}' updated successfully for membership ID: {}", fieldName, membershipId);
        } catch (Exception e) {
            logger.error("Error updating membership field '{}' for membership ID: {}", fieldName, membershipId, e.getMessage());
            throw new RuntimeException("Error updating membership field: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteMembership(String membershipId) {
        if (!firebaseService.documentExists("memberships", membershipId)) {
            throw new IllegalArgumentException("Membership with ID " + membershipId + " does not exist.");
        }

        membershipLocks.putIfAbsent(membershipId, new ReentrantLock());
        Lock lock = membershipLocks.get(membershipId);

        lock.lock();
        try {
            firebaseService.deleteMembership(membershipId);
            logger.info("Membership deleted successfully: {}", membershipId);
        } catch (Exception e) {
            logger.error("Error deleting membership with ID: {}", membershipId, e.getMessage());
            throw new RuntimeException("Error deleting membership: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    // ----------------------- Fees Management -----------------------

    public void updateFeeField(String feeId, String fieldName, Object value) {
        if (!firebaseService.documentExists("fees", feeId)) {
            throw new IllegalArgumentException("Fee with ID " + feeId + " does not exist.");
        }

        String lockKey = "fee:" + feeId + ":" + fieldName;
        feeLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = feeLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("fees", feeId, fieldName, value);
            logger.info("Fee field '{}' updated successfully for fee ID: {}", fieldName, feeId);
        } catch (Exception e) {
            logger.error("Error updating fee field '{}' for fee ID: {}", fieldName, feeId, e.getMessage());
            throw new RuntimeException("Error updating fee field: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteFee(String feeId) {
        if (!firebaseService.documentExists("fees", feeId)) {
            throw new IllegalArgumentException("Fee with ID " + feeId + " does not exist.");
        }

        feeLocks.putIfAbsent(feeId, new ReentrantLock());
        Lock lock = feeLocks.get(feeId);

        lock.lock();
        try {
            firebaseService.deleteFee(feeId);
            logger.info("Fee deleted successfully: {}", feeId);
        } catch (Exception e) {
            logger.error("Error deleting fee with ID: {}", feeId, e.getMessage());
            throw new RuntimeException("Error deleting fee: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

// ----------------------- Borrows Management -----------------------

    public void addBorrow(Borrows borrow) {
        try {
            if (!firebaseService.documentExists("books", borrow.getBookId())) {
                throw new IllegalArgumentException("Book with ID " + borrow.getBookId() + " does not exist.");
            }
            if (!firebaseService.documentExists("memberships", borrow.getMembershipId())) {
                throw new IllegalArgumentException("Membership with ID " + borrow.getMembershipId() + " does not exist.");
            }
            firebaseService.addBorrow(borrow);
            logger.info("Borrow record added successfully: {}", borrow.getId());
        } catch (Exception e) {
            logger.error("Error adding borrow record: {}", e.getMessage(), e.getMessage());
            throw new RuntimeException("Error adding borrow record: " + e.getMessage());
        }
    }

    public void updateBorrow(String borrowId, Borrows updatedBorrow) {
        if (!firebaseService.documentExists("borrows", borrowId)) {
            throw new IllegalArgumentException("Borrow record with ID " + borrowId + " does not exist.");
        }

        String lockKey = "borrow:" + borrowId;
        feeLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = feeLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateBorrow(borrowId, updatedBorrow);
            logger.info("Borrow record updated successfully: {}", borrowId);
        } catch (Exception e) {
            logger.error("Error updating borrow record with ID: {}", borrowId, e.getMessage());
            throw new RuntimeException("Error updating borrow record: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }



}