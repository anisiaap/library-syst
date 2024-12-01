package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model .*;
import com.example.bureaucratic_system_backend.service.AdminService;
import com.example.bureaucratic_system_backend.service.BookLoaningService;
import com.example.bureaucratic_system_backend.service.FeeService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation .*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

    @RestController
    @RequestMapping("/api/admin")
    public class AdminController {

        private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

        @Autowired
        private AdminService adminService;

        @Autowired
        private BookLoaningService bookLoaningService;

        @Autowired
        private FeeService feeService;

        // Utility method to extract role from Firebase token
        private String extractRoleFromToken(String token) throws Exception {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token.replace("Bearer ", ""));
            return (String) decodedToken.getClaims().get("role");
        }

        // ----------------------- Configuration -----------------------

        @PostMapping("/config")
        public ResponseEntity<String> configureOffices(@RequestHeader("Authorization") String token, @RequestBody JsonObject configJson) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                List<Office> offices = parseConfiguration(configJson);
                return ResponseEntity.ok("Configuration received and loaded successfully");
            } catch (Exception e) {
                logger.error("Error configuring offices: {}", e.getMessage());
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        private List<Office> parseConfiguration(JsonObject configJson) {
            List<Office> parsedOffices = new ArrayList<>();
            JsonArray officeArray = configJson.getAsJsonArray("offices");

            for (JsonElement officeElement : officeArray) {
                JsonObject officeObj = officeElement.getAsJsonObject();
                String officeName = officeObj.get("name").getAsString();
                int counters = officeObj.get("counters").getAsInt();

                List<Document> documents = new ArrayList<>();
                JsonArray documentArray = officeObj.getAsJsonArray("documents");

                for (JsonElement documentElement : documentArray) {
                    JsonObject docObj = documentElement.getAsJsonObject();
                    String docName = docObj.get("name").getAsString();

                    List<String> dependencies = new ArrayList<>();
                    JsonArray depArray = docObj.getAsJsonArray("dependencies");
                    for (JsonElement depElement : depArray) {
                        dependencies.add(depElement.getAsString());
                    }

                    documents.add(new Document(docName, dependencies));
                }

                parsedOffices.add(new Office(officeName, counters, documents));
            }

            return parsedOffices;
        }

        // ----------------------- Counter Management -----------------------

        @PostMapping("/pause-counter")
        public ResponseEntity<String> pauseCounter(@RequestHeader("Authorization") String token, @RequestBody BreakTime breakTime) {
            return handleCounterAction(token, breakTime, true);
        }

        @PostMapping("/resume-counter")
        public ResponseEntity<String> resumeCounter(@RequestHeader("Authorization") String token, @RequestBody BreakTime breakTime) {
            return handleCounterAction(token, breakTime, false);
        }

        private ResponseEntity<String> handleCounterAction(String token, BreakTime breakTime, boolean isPauseAction) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String departmentName = breakTime.getDepartment();
                int counterId = breakTime.getCounterId();

                if ("BookLoaningDepartment".equals(departmentName)) {
                    if (isPauseAction) {
                        bookLoaningService.pauseCounter(counterId);
                        return ResponseEntity.ok("Counter " + counterId + " paused for a coffee break.");
                    } else {
                        bookLoaningService.resumeCounter(counterId);
                        return ResponseEntity.ok("Counter " + counterId + " resumed work.");
                    }
                } else {
                    return ResponseEntity.status(404).body("Department not found.");
                }
            } catch (Exception e) {
                logger.error("Error handling counter action: {}", e.getMessage());
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }

        // ----------------------- Book Management -----------------------

        @GetMapping("/books")
        public ResponseEntity<?> getAllBooks(@RequestHeader("Authorization") String token) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                return ResponseEntity.ok(adminService.getAllBooks());
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @PostMapping("/add-book")
        public ResponseEntity<String> addBook(@RequestHeader("Authorization") String token, @RequestBody Book book) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                adminService.addBook(book);
                return ResponseEntity.ok("Book added successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @PutMapping("/update-book")
        public ResponseEntity<String> updateBook(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> updateRequest) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String bookId = (String) updateRequest.get("bookId");
                String fieldName = (String) updateRequest.get("fieldName");
                Object value = updateRequest.get("value");

                adminService.updateBookField(bookId, fieldName, value);
                return ResponseEntity.ok("Book updated successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @DeleteMapping("/delete-book/{bookId}")
        public ResponseEntity<String> deleteBook(@RequestHeader("Authorization") String token, @PathVariable String bookId) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                adminService.deleteBook(bookId);
                return ResponseEntity.ok("Book deleted successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        // ----------------------- Citizen Management -----------------------

        @PostMapping("/add-citizen")
        public ResponseEntity<String> addCitizen(@RequestHeader("Authorization") String token, @RequestBody Citizen citizen) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                adminService.addCitizen(citizen);
                return ResponseEntity.ok("Citizen added successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @PutMapping("/update-citizen")
        public ResponseEntity<String> updateCitizen(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> updateRequest) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String citizenId = (String) updateRequest.get("citizenId");
                String fieldName = (String) updateRequest.get("fieldName");
                Object value = updateRequest.get("value");

                adminService.updateCitizenField(citizenId, fieldName, value);
                return ResponseEntity.ok("Citizen updated successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @DeleteMapping("/delete-citizen/{citizenId}")
        public ResponseEntity<String> deleteCitizen(@RequestHeader("Authorization") String token, @PathVariable String citizenId) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                adminService.deleteCitizen(citizenId);
                return ResponseEntity.ok("Citizen deleted successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        // ----------------------- Fee Management -----------------------

        @PostMapping("/add-fee")
        public ResponseEntity<String> addFee(@RequestHeader("Authorization") String token, @RequestBody Fees fee) {
            try {
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                feeService.addFee(fee);
                return ResponseEntity.ok("Fee added successfully.");
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
        }

        @GetMapping("/fees/{borrowId}")
        public ResponseEntity<?> getFeeByBorrowId(@RequestHeader("Authorization") String token, @PathVariable String borrowId) {
            try {
                // Check role-based access control
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

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

        @PutMapping("/update-fee")
        public ResponseEntity<String> updateFee(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> updateRequest) {
            try {
                // Check role-based access control
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String feeId = (String) updateRequest.get("feeId");
                String fieldName = (String) updateRequest.get("fieldName");
                Object value = updateRequest.get("value");

                adminService.updateFeeField(feeId, fieldName, value);
                return ResponseEntity.ok("Fee updated successfully.");
            } catch (Exception e) {
                logger.error("Error updating fee: {}", e.getMessage());
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }

        @DeleteMapping("/delete-fee/{feeId}")
        public ResponseEntity<String> deleteFee(@RequestHeader("Authorization") String token, @PathVariable String feeId) {
            try {
                // Check role-based access control
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                feeService.deleteFee(feeId);
                return ResponseEntity.ok("Fee deleted successfully.");
            } catch (Exception e) {
                logger.error("Error deleting fee with ID {}: {}", feeId, e.getMessage());
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }

        @PostMapping("/mark-fee-as-paid")
        public ResponseEntity<String> markFeeAsPaid(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
            try {
                // Check role-based access control
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String feeId = request.get("feeId");
                feeService.markFeeAsPaid(feeId);
                return ResponseEntity.ok("Fee marked as paid successfully.");
            } catch (Exception e) {
                logger.error("Error marking fee as paid: {}", e.getMessage());
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }

        @PostMapping("/generate-overdue-fee")
        public ResponseEntity<String> generateOverdueFee(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
            try {
                // Check role-based access control
                if (!"admin".equals(extractRoleFromToken(token))) {
                    return ResponseEntity.status(403).body("Access denied: Admins only.");
                }

                String borrowId = request.get("borrowId");
                feeService.generateOverdueFee(borrowId);
                return ResponseEntity.ok("Overdue fee generated successfully.");
            } catch (Exception e) {
                logger.error("Error generating overdue fee for borrow ID {}: {}", request.get("borrowId"), e.getMessage());
                return ResponseEntity.status(500).body("Internal server error.");
            }
        }
    }
