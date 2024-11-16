package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Membership;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FirebaseService {

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    public String getMembershipIdById(String citizenId) {
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

    public boolean borrowBook(String bookId, String membershipId) {
        DocumentReference bookRef = getFirestore().collection("books").document(bookId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("available", false);
        updates.put("borrowedBy", membershipId);
        updates.put("borrowDate", "2024-10-24");

        try {
            WriteResult result = bookRef.update(updates).get();
            System.out.println("Book borrowed successfully: " + result.getUpdateTime());
            return true;
        } catch (Exception e) {
            System.err.println("Error borrowing book: " + e.getMessage());
            return false;
        }
    }

    public void updateBook(Book book) {
        DocumentReference bookRef = getFirestore().collection("books").document(book.getId());
        Map<String, Object> updates = new HashMap<>();
        updates.put("available", book.isAvailable());
        updates.put("borrowedBy", book.getBorrowedBy());
        updates.put("borrowDate", book.getBorrowDate());

        try {
            WriteResult result = bookRef.update(updates).get();
            System.out.println("Book updated successfully: " + result.getUpdateTime());
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
        }
    }

    public Book getBookByTitleAndAuthor(String title, String author) {
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

    public void addMembership(Membership newMembership) {
        Map<String, Object> membershipData = new HashMap<>();
        membershipData.put("membershipNumber", newMembership.getMembershipNumber());
        membershipData.put("citizenName", newMembership.getCitizenName());
        membershipData.put("issueDate", newMembership.getIssueDate());
        membershipData.put("citizenId", newMembership.getCitizenId());

        try {
            WriteResult result = getFirestore().collection("memberships")
                    .document(newMembership.getMembershipNumber()).set(membershipData).get();
            System.out.println("Membership added successfully: " + result.getUpdateTime());
        } catch (Exception e) {
            System.err.println("Error adding membership: " + e.getMessage());
        }
    }

    public void addOrUpdateDocumentForCitizen(String citizenId, String documentName) {
        DocumentReference docRef = getFirestore().collection("citizenDocuments").document(citizenId);

        try {
            getFirestore().runTransaction(trans -> {
                DocumentSnapshot snapshot = trans.get(docRef).get();
                List<String> documents;
                if (snapshot.exists() && snapshot.contains("documentNames")) {
                    documents = (List<String>) snapshot.get("documentNames");
                    if (!documents.contains(documentName)) {
                        documents.add(documentName);
                    }
                } else {
                    documents = new ArrayList<>();
                    documents.add(documentName);
                }
                trans.set(docRef, Collections.singletonMap("documentNames", documents), SetOptions.merge());
                return null;
            }).get();
            System.out.println("Document added/updated successfully for citizen: " + citizenId);
        } catch (Exception e) {
            System.err.println("Error during transaction: " + e.getMessage());
        }
    }

    public boolean hasDocument(String citizenId, String documentName) {
        DocumentReference docRef = getFirestore().collection("citizenDocuments").document(citizenId);
        try {
            DocumentSnapshot documentSnapshot = docRef.get().get();
            if (documentSnapshot.exists()) {
                List<String> documents = (List<String>) documentSnapshot.get("documentNames");
                return documents != null && documents.contains(documentName);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking document: " + e.getMessage());
            return false;
        }
    }

    public List<Book> getAllBooksFromFirestore() {
        Firestore firestore = FirestoreClient.getFirestore();
        CollectionReference booksCollection = firestore.collection("books");

        List<Book> books = new ArrayList<>();
        try {
            ApiFuture<QuerySnapshot> querySnapshotApiFuture = booksCollection.get();
            List<QueryDocumentSnapshot> documents = querySnapshotApiFuture.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                books.add(document.toObject(Book.class));
            }
        } catch (Exception e) {
            System.err.println("Error retrieving books: " + e.getMessage());
        }
        return books;
    }
}
