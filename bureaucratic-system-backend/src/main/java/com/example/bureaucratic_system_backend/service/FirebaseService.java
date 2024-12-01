package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FirebaseService {

    private static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
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

    public static boolean borrowBook(String bookId, String membershipId) {
        DocumentReference bookRef = getFirestore().collection("books").document(bookId);
        CollectionReference borrowCollection = getFirestore().collection("borrows");

        Map<String, Object> bookUpdates = new HashMap<>();
        Map<String, Object> borrowData = new HashMap<>();
        bookUpdates.put("available", false);
        borrowData.put("membershipId", membershipId);
        borrowData.put("bookId", bookId);
        borrowData.put("borrowDate", LocalDate.now().toString());
        borrowData.put("dueDate", LocalDate.now().plusDays(30).toString());

        try {
            // Update the book's availability
            WriteResult bookResult = bookRef.update(bookUpdates).get();

            // Create a new borrow record with an auto-generated ID
            DocumentReference newBorrowRef = borrowCollection.document(); // Auto-generated ID instead of book id to avoid overwriting
            borrowData.put("id", newBorrowRef.getId()); // store the generated ID in the borrow record
            WriteResult borrowResult = newBorrowRef.set(borrowData).get();

            System.out.println("Book borrowed successfully: " + bookResult.getUpdateTime() +
                    ", Borrow record created with ID: " + newBorrowRef.getId());
            return true;
        } catch (Exception e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            return false;
        }
    }


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

    public void updateBookField(String bookId, String fieldName, Object value) {
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

    public List<Fees> getFeesByMembershipId(String membershipId) {
        try {
            // Query Firestore for documents with the specific membership ID
            ApiFuture<QuerySnapshot> query = getFirestore().collection("fees")
                    .whereEqualTo("membershipId", membershipId).get(); // Match "membershipId" field
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();

            // Convert each document to a Fees object and return as a list
            List<Fees> feesList = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                feesList.add(document.toObject(Fees.class));
            }
            return feesList;
        } catch (Exception e) {
            System.err.println("Error fetching fees by membership ID: " + e.getMessage());
            return new ArrayList<>(); // Return an empty list if an error occurs
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

    // ----------------------- General -----------------------

    public void updateField(String collectionName, String documentId, String fieldName, Object value) {
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
}