package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.*;
import com.example.bureaucratic_system_backend.service.BookLoaningService;
import com.example.bureaucratic_system_backend.service.EnrollmentDepartmentService;
import com.example.bureaucratic_system_backend.service.FirebaseService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

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
        Citizen citizen = new Citizen();
        citizen.setId(loanRequest.getCitizenId());
        bookLoaningService.addCitizenToQueue(citizen, loanRequest.getBookTitle(), loanRequest.getBookAuthor());
        return ResponseEntity.ok("Citizen added to queue for loan request");
    }

    @PostMapping("/pause-counter")
    public ResponseEntity<String> pauseCounter(@RequestBody JsonObject jsonObject) {
        String departmentName = jsonObject.has("department") ? jsonObject.get("department").getAsString() : null;
        String counterIdParam = jsonObject.has("counterId") ? jsonObject.get("counterId").getAsString() : null;

        if (departmentName == null || counterIdParam == null) {
            return ResponseEntity.badRequest().body("Missing required parameters: department and/or counterId");
        }

        try {
            int counterId = Integer.parseInt(counterIdParam);
            if ("BookLoaningDepartment".equals(departmentName)) {
                bookLoaningService.pauseCounter(counterId);
                return ResponseEntity.ok("Counter " + counterId + " in " + departmentName + " paused for a coffee break");
            } else {
                return ResponseEntity.status(404).body("Department not found");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid counterId format. Must be an integer.");
        }
    }

    @PostMapping("/resume-counter")
    public ResponseEntity<String> resumeCounter(@RequestBody JsonObject jsonObject) {
        String departmentName = jsonObject.has("department") ? jsonObject.get("department").getAsString() : null;
        String counterIdParam = jsonObject.has("counterId") ? jsonObject.get("counterId").getAsString() : null;

        if (departmentName == null || counterIdParam == null) {
            return ResponseEntity.badRequest().body("Missing required parameters: department and/or counterId");
        }

        try {
            int counterId = Integer.parseInt(counterIdParam);
            if ("BookLoaningDepartment".equals(departmentName)) {
                bookLoaningService.resumeCounter(counterId);
                return ResponseEntity.ok("Counter " + counterId + " in " + departmentName + " resumed work");
            } else {
                return ResponseEntity.status(404).body("Department not found");
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid counterId format. Must be an integer.");
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
