package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.*;
import com.example.bureaucratic_system_backend.service.BookLoaningService;
import com.example.bureaucratic_system_backend.service.EnrollmentDepartmentService;
import com.example.bureaucratic_system_backend.service.FirebaseService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(BookLoaningService.class);

    @Autowired
    private BookLoaningService bookLoaningService;

    @Autowired
    private EnrollmentDepartmentService enrollmentDepartmentService;

    @Autowired
    private FirebaseService firebaseService;

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            // Retrieve all books using Firestore query
            List<Book> books = firebaseService.getAllBooksFromFirestore();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }

    @PostMapping("/loan-request")
    public ResponseEntity<String> processLoanRequest(@RequestBody LoanRequest loanRequest) {
        Map<String, Department> departments = new HashMap<>();
        // Use the injected service directly without calling getInstance()
        departments.put("BookLoaningDepartment", bookLoaningService);

        Citizen citizen = new Citizen();
        citizen.setId(loanRequest.getCitizenId());
        bookLoaningService.addCitizenToQueue(citizen, loanRequest.getBookTitle(), loanRequest.getBookAuthor());
        return ResponseEntity.ok("Citizen added to queue for loan request");
    }

    @PostMapping("/pause-counter")
    public ResponseEntity<String> pauseCounter(@RequestBody BreakTime breakTime) {
        return handleCounterAction(breakTime, true);
    }

    @PostMapping("/resume-counter")
    public ResponseEntity<String> resumeCounter(@RequestBody BreakTime breakTime) {
        return handleCounterAction(breakTime, false);
    }

    private ResponseEntity<String> handleCounterAction(BreakTime breakTime, boolean isPauseAction) {
        String departmentName = breakTime.getDepartment();
        int counterId = breakTime.getCounterId();

        logger.info("Request received for {} action. {}", isPauseAction ? "pause" : "resume", breakTime);

        if (departmentName == null || departmentName.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required parameter: department");
        }

        try {
            if ("BookLoaningDepartment".equals(departmentName)) {
                if (isPauseAction) {
                    bookLoaningService.pauseCounter(counterId);
                    return ResponseEntity.ok("Counter " + counterId + " in " + departmentName + " paused for a coffee break");
                } else {
                    bookLoaningService.resumeCounter(counterId);
                    return ResponseEntity.ok("Counter " + counterId + " in " + departmentName + " resumed work");
                }
            } else {
                return ResponseEntity.status(404).body("Department not found");
            }
        } catch (Exception e) {
            logger.error("Error processing request for {}: {}", breakTime, e.getMessage(), e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }


    @PostMapping("/enroll")
    public ResponseEntity<String> enrollCitizen(@RequestBody Citizen citizen) {
        boolean success = enrollmentDepartmentService.enrollCitizen(citizen);
        return success
                ? ResponseEntity.ok("Citizen enrolled successfully")
                : ResponseEntity.badRequest().body("Citizen enrollment failed");
    }

    @PostMapping("/config")
    public ResponseEntity<String> configureOffices(@RequestBody JsonObject configJson) {
        List<Office> offices = parseConfiguration(configJson);
        return ResponseEntity.ok("Configuration received and loaded successfully");
    }

    // Parsing the configuration JSON
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
}