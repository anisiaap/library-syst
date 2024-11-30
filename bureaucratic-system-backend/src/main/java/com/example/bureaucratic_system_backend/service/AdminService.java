package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Borrows;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Membership;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AdminService {

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

    public List<Book> getAllBooks() {
        try {
            return firebaseService.getAllBooksFromFirestore();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving books: " + e.getMessage(), e);
        }
    }

    public void addBook(Book book) {
        try {
            firebaseService.addBook(book);
            System.out.println("Book added successfully: " + book.getName());
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
        }
    }

    public void updateBookField(String bookId, String fieldName, Object value) {
        String lockKey = "book:" + bookId + ":" + fieldName;
        bookLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = bookLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("books", bookId, fieldName, value);
            System.out.println("Book field '" + fieldName + "' updated successfully for book ID: " + bookId);
        } catch (Exception e) {
            System.err.println("Error updating book field '" + fieldName + "' for book ID: " + bookId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteBook(String bookId) {
        bookLocks.putIfAbsent(bookId, new ReentrantLock());
        Lock lock = bookLocks.get(bookId);

        lock.lock();
        try {
            firebaseService.deleteBook(bookId);
            System.out.println("Book deleted successfully: " + bookId);
        } catch (Exception e) {
            System.err.println("Error deleting book with ID: " + bookId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ----------------------- Citizens Management -----------------------

    public void addCitizen(Citizen citizen) {
        try {
            firebaseService.addCitizen(citizen);
            System.out.println("Citizen added successfully: " + citizen.getName());
        } catch (Exception e) {
            System.err.println("Error adding citizen: " + e.getMessage());
        }
    }

    public void updateCitizenField(String citizenId, String fieldName, Object value) {
        String lockKey = "citizen:" + citizenId + ":" + fieldName;
        citizenLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = citizenLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("citizen", citizenId, fieldName, value);
            System.out.println("Citizen field '" + fieldName + "' updated successfully for citizen ID: " + citizenId);
        } catch (Exception e) {
            System.err.println("Error updating citizen field '" + fieldName + "' for citizen ID: " + citizenId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteCitizen(String citizenId) {
        citizenLocks.putIfAbsent(citizenId, new ReentrantLock());
        Lock lock = citizenLocks.get(citizenId);

        lock.lock();
        try {
            firebaseService.deleteCitizen(citizenId);
            System.out.println("Citizen deleted successfully: " + citizenId);
        } catch (Exception e) {
            System.err.println("Error deleting citizen with ID: " + citizenId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ----------------------- Memberships Management -----------------------

    public void addMembership(Membership membership) {
        try {
            firebaseService.addMembership(membership);
            System.out.println("Membership added successfully: " + membership.getMembershipNumber());
        } catch (Exception e) {
            System.err.println("Error adding membership: " + e.getMessage());
        }
    }

    public void updateMembershipField(String membershipId, String fieldName, Object value) {
        String lockKey = "membership:" + membershipId + ":" + fieldName;
        membershipLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = membershipLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("memberships", membershipId, fieldName, value);
            System.out.println("Membership field '" + fieldName + "' updated successfully for membership ID: " + membershipId);
        } catch (Exception e) {
            System.err.println("Error updating membership field '" + fieldName + "' for membership ID: " + membershipId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteMembership(String membershipId) {
        membershipLocks.putIfAbsent(membershipId, new ReentrantLock());
        Lock lock = membershipLocks.get(membershipId);

        lock.lock();
        try {
            firebaseService.deleteMembership(membershipId);
            System.out.println("Membership deleted successfully: " + membershipId);
        } catch (Exception e) {
            System.err.println("Error deleting membership with ID: " + membershipId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    // ----------------------- Fees Management -----------------------

    public void updateFeeField(String feeId, String fieldName, Object value) {
        String lockKey = "fee:" + feeId + ":" + fieldName;
        feeLocks.putIfAbsent(lockKey, new ReentrantLock());
        Lock lock = feeLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateField("fees", feeId, fieldName, value);
            System.out.println("Fee field '" + fieldName + "' updated successfully for fee ID: " + feeId);
        } catch (Exception e) {
            System.err.println("Error updating fee field '" + fieldName + "' for fee ID: " + feeId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteFee(String feeId) {
        feeLocks.putIfAbsent(feeId, new ReentrantLock());
        Lock lock = feeLocks.get(feeId);

        lock.lock();
        try {
            firebaseService.deleteFee(feeId);
            System.out.println("Fee deleted successfully: " + feeId);
        } catch (Exception e) {
            System.err.println("Error deleting fee with ID: " + feeId + ": " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    // ----------------------- Borrows Management -----------------------

    public void addBorrow(Borrows borrow) {
        try {
            firebaseService.addBorrow(borrow);
            System.out.println("Borrow record added successfully: " + borrow.getId());
        } catch (Exception e) {
            System.err.println("Error adding borrow record: " + e.getMessage());
        }
    }

    public void updateBorrow(String borrowId, Borrows updatedBorrow) {
        String lockKey = "borrow:" + borrowId;
        feeLocks.putIfAbsent(lockKey, new ReentrantLock()); // Reuse feeLocks for simplicity
        Lock lock = feeLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.updateBorrow(borrowId, updatedBorrow);
            System.out.println("Borrow record updated successfully: " + borrowId);
        } catch (Exception e) {
            System.err.println("Error updating borrow record: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void deleteBorrow(String borrowId) {
        String lockKey = "borrow:" + borrowId;
        feeLocks.putIfAbsent(lockKey, new ReentrantLock()); // Reuse feeLocks for simplicity
        Lock lock = feeLocks.get(lockKey);

        lock.lock();
        try {
            firebaseService.deleteBorrow(borrowId);
            System.out.println("Borrow record deleted successfully: " + borrowId);
        } catch (Exception e) {
            System.err.println("Error deleting borrow record: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }


}