package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Membership;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class FirebaseService {

    private static Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

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

    public static boolean borrowBook(String bookId, String membershipId) {
        DocumentReference bookRef = getFirestore().collection("books").document(bookId);

        DocumentReference bookRef2 = getFirestore().collection("borrows").document(bookId);
        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> updates2 = new HashMap<>();
        updates.put("available", false);
        //TODO instantiate a Borrow object
        updates2.put("membershipId", membershipId);
        updates2.put("bookId", bookId);
        updates2.put("borrowDate", LocalDate.now().toString());
        updates2.put("dueDate",LocalDate.now().plusDays(30).toString());

        try {
            WriteResult result = bookRef.update(updates).get();
            WriteResult result2 = getFirestore().collection("borrows")//TODO call the borrow object
                    .document(bookId).set(updates2).get();// instead of book id - borrowId
            System.out.println("Book borrowed successfully: " + result.getUpdateTime() +result2.getUpdateTime());
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

    public void addMembership(Membership newMembership) {
        Map<String, Object> membershipData = new HashMap<>();
        membershipData.put("id", newMembership.getMembershipNumber());
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
