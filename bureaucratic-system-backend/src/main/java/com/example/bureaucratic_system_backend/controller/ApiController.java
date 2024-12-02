package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Fees;
import com.example.bureaucratic_system_backend.model.LoanRequest;
import com.example.bureaucratic_system_backend.service.BookLoaningService;
import com.example.bureaucratic_system_backend.service.CitizenService;
import com.example.bureaucratic_system_backend.service.EnrollmentDepartmentService;
import com.example.bureaucratic_system_backend.service.FeeService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citizens")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private BookLoaningService bookLoaningService;
    @Autowired
    private CitizenService citizenService;
    @Autowired
    private FeeService feeService;
    @Autowired
    private EnrollmentDepartmentService enrollmentDepartmentService;

    private String extractRoleFromToken(String token) throws Exception {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.replace("Bearer ", ""));
        return (String) decodedToken.getClaims().get("role");
    }


    @PostMapping("/create-citizen")
    public ResponseEntity<String> createCitizen(@RequestBody Citizen citizen) {
        try {
            citizenService.addCitizen(citizen);
            return ResponseEntity.ok("Citizen created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating citizen: " + e.getMessage());
        }
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollCitizen(@RequestHeader("Authorization") String token, @RequestBody Citizen citizen) {
        try {
            if (!"citizen".equals(extractRoleFromToken(token))) {
                return ResponseEntity.status(403).body("Access denied: citizen only.");
            }

            boolean success = enrollmentDepartmentService.enrollCitizen(citizen);
            return success ? ResponseEntity.ok("Citizen enrolled successfully.") : ResponseEntity.badRequest().body("Enrollment failed.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }

    @PostMapping("/loan-request")
    public ResponseEntity<String> processLoanRequest(@RequestHeader("Authorization") String token, @RequestBody LoanRequest loanRequest) {
        try {
            if (!"citizen".equals(token)) {
                return ResponseEntity.status(403).body("Access denied: citizen only.");
            }

            Citizen citizen = new Citizen();
            citizen.setId(loanRequest.getCitizenId());
            bookLoaningService.addCitizenToQueue(citizen, loanRequest.getBookTitle(), loanRequest.getBookAuthor());
            return ResponseEntity.ok("Loan request processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
    }
    @GetMapping("/fees/{borrowId}")
    public ResponseEntity<?> getFeeByBorrowId(@RequestHeader("Authorization") String token, @PathVariable String borrowId) {
        try {

            Fees fee = feeService.getFeeByBorrowId(borrowId);
            if (fee == null) {
                return ResponseEntity.status(404).body("Fee not found for borrow ID: " + borrowId);
            }

            return ResponseEntity.ok(fee);
        } catch (Exception e) {
            logger.error("Error retrieving fee for borrow ID {}: {}", borrowId, e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
    @PostMapping("/mark-as-paid")
    public ResponseEntity<String> markFeeAsPaid(@RequestHeader("Authorization") String token, @RequestBody String feeId) {
        try {
            feeService.markFeeAsPaid(feeId);
            return ResponseEntity.ok("Fee marked as paid successfully.");
        } catch (Exception e) {
            logger.error("Error marking fee as paid for ID {}: {}", feeId, e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
}