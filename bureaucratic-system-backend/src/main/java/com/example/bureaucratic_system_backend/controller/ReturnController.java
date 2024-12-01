package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.ReturnRequest;
import com.example.bureaucratic_system_backend.service.ReturnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private static final Logger logger = LoggerFactory.getLogger(ReturnController.class);

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping("/return-book")
    public ResponseEntity<String> processReturn(@RequestBody ReturnRequest returnRequest) {
        try {
            returnService.processReturn(returnRequest.getMembershipId(),
                    returnRequest.getBookTitle(),
                    returnRequest.getBookAuthor());
            return ResponseEntity.ok("Book return processed successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing return: {}", e.getMessage());
            return ResponseEntity.status(500).body("Internal server error.");
        }
    }
}