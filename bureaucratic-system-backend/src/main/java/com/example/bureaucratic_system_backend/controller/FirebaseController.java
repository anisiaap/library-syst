package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Borrows;
import com.example.bureaucratic_system_backend.model.Fees;
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
        return FirebaseService.getMembershipIdById(citizenId);
    }
    // Enhanced /books endpoint

    @GetMapping("/books")
    public ResponseEntity<List<Map<String, Object>>> getAllBooks() {
        List<Map<String, Object>> groupedBooks = firebaseService.getAllBooksGroupedByAuthorAndName();
        return ResponseEntity.ok(groupedBooks);
    }
    @PostMapping("/memberships")
    public void addMembership(@RequestBody Membership membership) {
        firebaseService.addMembership(membership);
    }
    @GetMapping("/borrows/{membershipId}")
    public ResponseEntity<List<Borrows>> getBorrowsByMembershipId(@PathVariable String membershipId) {
        try {

            List<Borrows> borrowsList = firebaseService.getBorrowsByMembershipId(membershipId);
            return ResponseEntity.ok(borrowsList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
    @GetMapping("/users/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            // Use FirebaseService to fetch user data by email
            Map<String, Object> user = firebaseService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body("User not found for email: " + email);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching user: " + e.getMessage());
        }
    }

    // Fetch fees history by membership ID
    @GetMapping("/fees/{membershipId}")
    public ResponseEntity<List<Fees>> getFeesByMembershipId(@PathVariable String membershipId) {
        try {
            List<Fees> feesList = firebaseService.getFeesByMembershipId(membershipId);
            return ResponseEntity.ok(feesList);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
