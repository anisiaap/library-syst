package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FirebaseService {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseService.class);

    private static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }
    public void assignRole(String userId, String role) throws Exception {
        FirebaseAuth.getInstance().setCustomUserClaims(userId, Map.of("role", role));
    }
    public Map<String, Object> getUserByEmail(String email) {
        System.out.println("Querying Firestore for email: " + email);
        try {
            ApiFuture<QuerySnapshot> query = getFirestore()
                    .collection("users")
                    .whereEqualTo("email", email)
                    .get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            if (documents.isEmpty()) {
                System.out.println("No user found for email: " + email);
                return null;
            }
            return documents.get(0).getData();
        } catch (Exception e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
            return null;
        }
    }
    public static void saveCounterToFirestore(Counter counter) {
        try {
            Firestore firestore = getFirestore();
            firestore.collection("counters").document(String.valueOf(counter.getCounterId()))
                    .set(Map.of(
                            "counterId", counter.getCounterId(),
                            "isPaused", counter.isPaused()
                    )).get();
            logger.info("Counter {} initialized in Firestore.", counter.getCounterId());
        } catch (Exception e) {
            logger.error("Error saving counter to Firestore: {}", e.getMessage());
        }
    }
    public static Map<String, Object> getCounterById(int counterId) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> query = firestore.collection("counters")
                    .whereEqualTo("counterId", counterId)
                    .get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            if (!documents.isEmpty()) {
                return documents.get(0).getData(); // Return the first matching counter
            } else {
                System.err.println("Counter not found for ID: " + counterId);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching counter by ID: " + e.getMessage());
            return null;
        }
    }
    public static void listenToCounterChanges(Map<Integer, Object> counterLocks, Map<Integer, Boolean> counterPauseStatus) {
        Firestore firestore = getFirestore();
        firestore.collection("counters").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                System.err.println("Error listening to counters: " + e.getMessage());
                return;
            }

            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    Map<String, Object> counterData = dc.getDocument().getData();
                    Integer counterId = (counterData.containsKey("counterId"))
                            ? ((Long) counterData.get("counterId")).intValue()
                            : null;
                    Boolean isPaused = (Boolean) counterData.get("isPaused");

                    if (counterId != null) {
                        counterPauseStatus.put(counterId, isPaused);

                        // Notify the thread if the counter is no longer paused
                        if (!isPaused) {
                            synchronized (counterLocks.get(counterId)) {
                                counterLocks.get(counterId).notifyAll();
                            }
                            System.out.printf("Counter %d resumed from Firestore changes.%n", counterId);
                        }
                    }
                }
            }
        });
    }
    public static void updateCounterState(int counterId, boolean isPaused) {
        try {
            Firestore firestore = getFirestore();
            firestore.collection("counters").document(String.valueOf(counterId))
                    .update("isPaused", isPaused).get();
            logger.info("Counter {} state updated to {}.", counterId, isPaused ? "Paused" : "Active");
        } catch (Exception e) {
            logger.error("Error updating counter state in Firestore: {}", e.getMessage());
        }
    }
    public static List<Counter> getAllCounters() {
        List<Counter> counters = new ArrayList<>();
        try {
            Firestore firestore = getFirestore();
            ApiFuture<QuerySnapshot> query = firestore.collection("counters").get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                int counterId = document.getLong("counterId").intValue();
                boolean isPaused = document.getBoolean("isPaused");
                counters.add(new Counter(counterId, isPaused));
            }
        } catch (Exception e) {
            logger.error("Error fetching counters from Firestore: {}", e.getMessage());
        }
        return counters;
    }
    public static void clearCountersCollection() {
        Firestore firestore = getFirestore();

        try {
            ApiFuture<QuerySnapshot> query = firestore.collection("counters").get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                firestore.collection("counters").document(document.getId()).delete();
                logger.info("Deleted counter document: {}", document.getId());
            }

            logger.info("All counters cleared from Firestore.");
        } catch (Exception e) {
            logger.error("Error clearing counters collection: {}", e.getMessage());
        }
    }
    // ----------------------- Memberships -----------------------

    public static String getMembershipIdById(String citizenId) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("memberships")
                    .whereEqualTo("citizenId", citizenId).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            return documents.isEmpty() ? null : documents.get(0).getId();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addMembership(Membership newMembership) {
        Map<String, Object> membershipData = new HashMap<>();
        membershipData.put("id", newMembership.getMembershipNumber());
        membershipData.put("issueDate", newMembership.getIssueDate());
        membershipData.put("citizenId", newMembership.getCitizenId());

        try {
            getFirestore().collection("memberships").document(newMembership.getMembershipNumber()).set(membershipData).get();
            System.out.println("Membership added successfully: " + newMembership.getMembershipNumber());
        } catch (Exception e) {
            System.err.println("Error adding membership: " + e.getMessage());
        }
    }

    public void updateMembershipField(String membershipId, String fieldName, Object value) {
        updateField("memberships", membershipId, fieldName, value);
    }

    public void deleteMembership(String membershipId) {
        try {
            getFirestore().collection("memberships").document(membershipId).delete().get();
            System.out.println("Membership deleted successfully: " + membershipId);
        } catch (Exception e) {
            System.err.println("Error deleting membership: " + e.getMessage());
        }
    }

    // ----------------------- Books -----------------------

    public static Book getBookByTitleAndAuthor(String title, String author) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("books")
                    .whereEqualTo("name", title)
                    .whereEqualTo("author", author)
                    .whereEqualTo("available", true).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            return documents.isEmpty() ? null : documents.get(0).toObject(Book.class);
        } catch (Exception e) {
            System.err.println("Error fetching book: " + e.getMessage());
            return null;
        }
    }

    public void addBook(Book book) {
        try {
            getFirestore().collection("books").document(book.getId()).set(book).get();
            System.out.println("Book added successfully: " + book.getName());
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
        }
    }
    public List<String> getAllDocumentIds(String collectionName) {
        Firestore firestore = FirestoreClient.getFirestore();

        try {
            return firestore.collection(collectionName)
                    .get()
                    .get()
                    .getDocuments()
                    .stream()
                    .map(QueryDocumentSnapshot::getId) // Extract the document IDs
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching document IDs from collection {}: {}", collectionName, e.getMessage());
            throw new RuntimeException("Failed to fetch document IDs: " + e.getMessage(), e);
        }
    }

    public static void updateBookField(String bookId, String fieldName, Object value) {
        updateField("books", bookId, fieldName, value);
    }

    public void deleteBook(String bookId) {
        try {
            getFirestore().collection("books").document(bookId).delete().get();
            System.out.println("Book deleted successfully: " + bookId);
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
        }
    }

    public List<Book> getAllBooksFromFirestore() {
        List<Book> books = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("books").get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                books.add(document.toObject(Book.class));
            }
        } catch (Exception e) {
            System.err.println("Error retrieving books: " + e.getMessage());
        }
        return books;
    }

    // ----------------------- Citizens -----------------------

    public void addCitizen(Citizen citizen) {
        try {
            getFirestore().collection("citizen").document(citizen.getId()).set(citizen).get();
            System.out.println("Citizen added successfully: " + citizen.getName());
        } catch (Exception e) {
            System.err.println("Error adding citizen: " + e.getMessage());
        }
    }

    public void updateCitizenField(String citizenId, String fieldName, Object value) {
        updateField("citizen", citizenId, fieldName, value);
    }

    public void deleteCitizen(String citizenId) {
        try {
            getFirestore().collection("citizen").document(citizenId).delete().get();
            System.out.println("Citizen deleted successfully: " + citizenId);
        } catch (Exception e) {
            System.err.println("Error deleting citizen: " + e.getMessage());
        }
    }

    // ----------------------- Fees -----------------------

    public void addFee(Fees fee) {
        try {
            getFirestore().collection("fees").document(fee.getId()).set(fee).get();
            System.out.println("Fee added successfully: " + fee.getId());
        } catch (Exception e) {
            System.err.println("Error adding fee: " + e.getMessage());
        }
    }

    public void updateFee(String feeId, Fees updatedFee) {
        try {
            getFirestore().collection("fees").document(feeId).set(updatedFee).get();
            System.out.println("Fee updated successfully: " + feeId);
        } catch (Exception e) {
            System.err.println("Error updating fee: " + e.getMessage());
        }
    }

    public Fees getFeeByBorrowId(String borrowId) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("fees")
                    .whereEqualTo("borrowId", borrowId).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            return documents.isEmpty() ? null : documents.get(0).toObject(Fees.class);
        } catch (Exception e) {
            System.err.println("Error fetching fee: " + e.getMessage());
            return null;
        }
    }

    public Fees getFeeById(String feeId) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("fees")
                    .whereEqualTo("id", feeId).get(); // Match the "id" field with feeId
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            return documents.isEmpty() ? null : documents.get(0).toObject(Fees.class);
        } catch (Exception e) {
            System.err.println("Error fetching fee by ID: " + e.getMessage());
            return null;
        }
    }

    // Fetch all fees by membership ID
    public List<Fees> getFeesByMembershipId(String membershipId) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("fees")
                    .whereEqualTo("membershipId", membershipId).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            List<Fees> feesList = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                feesList.add(document.toObject(Fees.class));
            }
            return feesList;
        } catch (Exception e) {
            System.err.println("Error fetching fees by membership ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void deleteFee(String feeId) {
        try {
            getFirestore().collection("fees").document(feeId).delete().get();
            System.out.println("Fee deleted successfully: " + feeId);
        } catch (Exception e) {
            System.err.println("Error deleting fee: " + e.getMessage());
        }
    }

    // ----------------------- Borrows -----------------------

    public void addBorrow(Borrows borrow) {
        try {
            getFirestore().collection("borrows").document(borrow.getId()).set(borrow).get();
            System.out.println("Borrow record added successfully: " + borrow.getId());
        } catch (Exception e) {
            System.err.println("Error adding borrow record: " + e.getMessage());
        }
    }
    // Fetch all borrows by membership ID
    public List<Borrows> getBorrowsByMembershipId(String membershipId) {
        try {
            ApiFuture<QuerySnapshot> query = getFirestore().collection("borrows")
                    .whereEqualTo("membershipId", membershipId).get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            List<Borrows> borrowsList = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                borrowsList.add(document.toObject(Borrows.class));
            }
            return borrowsList;
        } catch (Exception e) {
            System.err.println("Error fetching borrows by membership ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public Borrows getBorrowById(String borrowId) {
        try {
            DocumentSnapshot snapshot = getFirestore().collection("borrows").document(borrowId).get().get();
            return snapshot.exists() ? snapshot.toObject(Borrows.class) : null;
        } catch (Exception e) {
            System.err.println("Error retrieving borrow record: " + e.getMessage());
            return null;
        }
    }

    public void updateBorrow(String borrowId, Borrows updatedBorrow) {
        try {
            getFirestore().collection("borrows").document(borrowId).set(updatedBorrow).get();
            System.out.println("Borrow record updated successfully: " + borrowId);
        } catch (Exception e) {
            System.err.println("Error updating borrow record: " + e.getMessage());
        }
    }

    public void deleteBorrow(String borrowId) {
        try {
            getFirestore().collection("borrows").document(borrowId).delete().get();
            System.out.println("Borrow record deleted successfully: " + borrowId);
        } catch (Exception e) {
            System.err.println("Error deleting borrow record: " + e.getMessage());
        }
    }
    public static Borrows getBorrowByMembershipAndBook(String membershipId, String bookTitle, String bookAuthor) {
        try {
            // Step 1: Fetch the Book ID from the Books collection
            ApiFuture<QuerySnapshot> bookQuery = getFirestore().collection("books")
                    .whereEqualTo("name", bookTitle)
                    .whereEqualTo("author", bookAuthor)
                    .get();

            List<QueryDocumentSnapshot> bookDocuments = bookQuery.get().getDocuments();
            if (bookDocuments.isEmpty()) {
                System.err.println("Book not found for title: " + bookTitle + ", author: " + bookAuthor);
                return null;
            }

            String bookId = bookDocuments.get(0).getId(); // Assuming book ID is the document ID

            // Step 2: Fetch the Borrow record using membershipId and bookId
            ApiFuture<QuerySnapshot> borrowQuery = getFirestore().collection("borrows")
                    .whereEqualTo("membershipId", membershipId)
                    .whereEqualTo("bookId", bookId)
                    .whereEqualTo("returnDate", null) // Ensure the book hasn't been returned yet
                    .get();

            List<QueryDocumentSnapshot> borrowDocuments = borrowQuery.get().getDocuments();
            if (borrowDocuments.isEmpty()) {
                System.err.println("No active borrow found for membershipId: " + membershipId + ", bookId: " + bookId);
                return null;
            }

            return borrowDocuments.get(0).toObject(Borrows.class);
        } catch (Exception e) {
            System.err.println("Error fetching borrow record: " + e.getMessage());
            return null;
        }
    }

    // ----------------------- General -----------------------

    public static void updateField(String collectionName, String documentId, String fieldName, Object value) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldName, value);

        try {
            getFirestore().collection(collectionName).document(documentId).update(updates).get();
            System.out.println(collectionName + " field '" + fieldName + "' updated successfully for ID: " + documentId);
        } catch (Exception e) {
            System.err.println("Error updating " + collectionName + " field '" + fieldName + "': " + e.getMessage());
        }
    }

    public boolean documentExists(String collectionName, String documentId) {
        try {
            DocumentSnapshot snapshot = getFirestore()
                    .collection(collectionName)
                    .document(documentId)
                    .get()
                    .get();
            return snapshot.exists();
        } catch (Exception e) {
            System.err.println("Error checking existence of document in " + collectionName + " with ID: " + documentId);
            return false;
        }
    }

    public Map<String, Object> getDocumentById(String collectionName, String documentId) {
        try {
            DocumentSnapshot snapshot = getFirestore()
                    .collection(collectionName)
                    .document(documentId)
                    .get()
                    .get();
            return snapshot.exists() ? snapshot.getData() : null;
        } catch (Exception e) {
            System.err.println("Error retrieving document in " + collectionName + " with ID: " + documentId);
            return null;
        }
    }
    public List<Map<String, Object>> getAllBooksGroupedByAuthorAndName() {
        // Fetch all books from Firestore
        List<Book> books = getAllBooksFromFirestore();

        // Group books by name and author, and calculate total available pieces
        return books.stream()
                .filter(Book::isAvailable) // Only include available books
                .collect(Collectors.groupingBy(
                        book -> Map.of("name", (Object) book.getName(), "author", (Object) book.getAuthor()), // Cast to Object
                        Collectors.counting() // Count available books in each group
                ))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> key = entry.getKey();
                    long totalPieces = entry.getValue();
                    return Map.of(
                            "name", key.get("name"),
                            "author", key.get("author"),
                            "totalPieces", totalPieces
                    );
                })
                .toList();
    }
}
