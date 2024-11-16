package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Membership;
import com.example.bureaucratic_system_backend.service.FirebaseService;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/books/{bookId}/borrow")
    public boolean borrowBook(@PathVariable String bookId, @RequestParam String membershipId) {
        return firebaseService.borrowBook(bookId, membershipId);
    }

    @PostMapping("/memberships")
    public void addMembership(@RequestBody Membership membership) {
        firebaseService.addMembership(membership);
    }
}
