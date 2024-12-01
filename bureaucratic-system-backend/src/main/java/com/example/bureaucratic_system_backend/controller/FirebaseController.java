package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Membership;
import com.example.bureaucratic_system_backend.service.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/firebase")
public class FirebaseController {

    private final FirebaseService firebaseService;

    public FirebaseController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @GetMapping("/memberships/{citizenId}")
    public String getMembershipIdById(@PathVariable String citizenId) {
        return firebaseService.getMembershipIdById(citizenId);
    }

//    @PostMapping("/books/{bookId}/borrow")
//    public boolean borrowBook(@PathVariable String bookId, @RequestParam String membershipId) {
//        return firebaseService.borrowBook(bookId, membershipId);
//    }
    // Extract role from token
    private String extractRoleFromToken(String token) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.replace("Bearer ", ""));
        return (String) decodedToken.getClaims().get("role");
    }
    // Enhanced /books endpoint
    @GetMapping("/books")
     public List<Map<String, Object>> getAllBooks(){

            // Fetch all books from Firestore
            List<Book> books = firebaseService.getAllBooksFromFirestore();

            // Group books by name and author, and calculate total available pieces
            List<Map<String, Object>> groupedBooks = books.stream()
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


            return groupedBooks;
    }
    @PostMapping("/memberships")
    public void addMembership(@RequestBody Membership membership) {
        firebaseService.addMembership(membership);
    }

}
