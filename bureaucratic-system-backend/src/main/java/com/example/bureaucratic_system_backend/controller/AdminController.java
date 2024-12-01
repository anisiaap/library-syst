package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Book;
import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Fees;
import com.example.bureaucratic_system_backend.model.Membership;
import com.example.bureaucratic_system_backend.service.AdminService;
import com.example.bureaucratic_system_backend.service.FeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private FeeService feeService;

    // ----------------------- Book Management -----------------------
    @GetMapping("/books")
    public ResponseEntity<?> getAllBooks() {
        try {
            return ResponseEntity.ok(adminService.getAllBooks());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving books: " + e.getMessage());
        }
    }

    @PostMapping("/add-book")
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        adminService.addBook(book);
        return ResponseEntity.ok("Book added successfully.");
    }

    @PutMapping("/update-book")
    public ResponseEntity<String> updateBook(@RequestBody Map<String, Object> updateRequest) {
        String bookId = (String) updateRequest.get("bookId");
        String fieldName = (String) updateRequest.get("fieldName");
        Object value = updateRequest.get("value");

        adminService.updateBookField(bookId, fieldName, value);
        return ResponseEntity.ok("Book updated successfully.");
    }

    @DeleteMapping("/delete-book/{bookId}")
    public ResponseEntity<String> deleteBook(@PathVariable String bookId) {
        adminService.deleteBook(bookId);
        return ResponseEntity.ok("Book deleted successfully.");
    }

    // ----------------------- Citizen Management -----------------------
    @PostMapping("/add-citizen")
    public ResponseEntity<String> addCitizen(@RequestBody Citizen citizen) {
        adminService.addCitizen(citizen);
        return ResponseEntity.ok("Citizen added successfully.");
    }

    @PutMapping("/update-citizen")
    public ResponseEntity<String> updateCitizen(@RequestBody Map<String, Object> updateRequest) {
        String citizenId = (String) updateRequest.get("citizenId");
        String fieldName = (String) updateRequest.get("fieldName");
        Object value = updateRequest.get("value");

        adminService.updateCitizenField(citizenId, fieldName, value);
        return ResponseEntity.ok("Citizen updated successfully.");
    }

    @DeleteMapping("/delete-citizen/{citizenId}")
    public ResponseEntity<String> deleteCitizen(@PathVariable String citizenId) {
        adminService.deleteCitizen(citizenId);
        return ResponseEntity.ok("Citizen deleted successfully.");
    }

    // ----------------------- Membership Management -----------------------
    @PostMapping("/add-membership")
    public ResponseEntity<String> addMembership(@RequestBody Membership membership) {
        adminService.addMembership(membership);
        return ResponseEntity.ok("Membership added successfully.");
    }

    @PutMapping("/update-membership")
    public ResponseEntity<String> updateMembership(@RequestBody Map<String, Object> updateRequest) {
        String membershipId = (String) updateRequest.get("membershipId");
        String fieldName = (String) updateRequest.get("fieldName");
        Object value = updateRequest.get("value");

        adminService.updateMembershipField(membershipId, fieldName, value);
        return ResponseEntity.ok("Membership updated successfully.");
    }

    @DeleteMapping("/delete-membership/{membershipId}")
    public ResponseEntity<String> deleteMembership(@PathVariable String membershipId) {
        adminService.deleteMembership(membershipId);
        return ResponseEntity.ok("Membership deleted successfully.");
    }

    // ----------------------- Fee Management -----------------------
    @PostMapping("/add-fee")
    public ResponseEntity<String> addFee(@RequestBody Fees fee) {
        feeService.addFee(fee);
        return ResponseEntity.ok("Fee added successfully.");
    }

    @GetMapping("/fees/{borrowId}")
    public ResponseEntity<Fees> getFeeByBorrowId(@PathVariable String borrowId) {
        Fees fee = feeService.getFeeByBorrowId(borrowId);
        if (fee == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(fee);
    }

    @PutMapping("/update-fee")
    public ResponseEntity<String> updateFee(@RequestBody Map<String, Object> updateRequest) {
        String feeId = (String) updateRequest.get("feeId");
        String fieldName = (String) updateRequest.get("fieldName");
        Object value = updateRequest.get("value");

        adminService.updateFeeField(feeId, fieldName, value);
        return ResponseEntity.ok("Fee updated successfully.");
    }

    @DeleteMapping("/delete-fee/{feeId}")
    public ResponseEntity<String> deleteFee(@PathVariable String feeId) {
        feeService.deleteFee(feeId);
        return ResponseEntity.ok("Fee deleted successfully.");
    }

    @PostMapping("/mark-fee-as-paid")
    public ResponseEntity<String> markFeeAsPaid(@RequestBody Map<String, String> request) {
        String feeId = request.get("feeId");
        feeService.markFeeAsPaid(feeId);
        return ResponseEntity.ok("Fee marked as paid.");
    }

    @PostMapping("/generate-overdue-fee")
    public ResponseEntity<String> generateOverdueFee(@RequestBody Map<String, String> request) {
        String borrowId = request.get("borrowId");
        feeService.generateOverdueFee(borrowId);
        return ResponseEntity.ok("Overdue fee generated successfully.");
    }
}
