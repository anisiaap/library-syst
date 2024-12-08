package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.service.BookLoaningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-loaning")
public class BookLoaningController {

    @Autowired
    private BookLoaningService bookLoaningService;

    @PostMapping("/add-to-queue")
    public String addCitizenToQueue(@RequestBody Citizen citizen, @RequestParam String bookTitle, @RequestParam String bookAuthor) {
        bookLoaningService.addCitizenToQueue(citizen, bookTitle, bookAuthor);
        return "Citizen added to the queue for book loaning.";
    }

    @PostMapping("/pause-counter/{counterId}")
    public String pauseCounter(@PathVariable int counterId) {
        bookLoaningService.pauseCounter(counterId);
        return "Counter " + counterId + " paused.";
    }

    @PostMapping("/resume-counter/{counterId}")
    public String resumeCounter(@PathVariable int counterId) {
        bookLoaningService.resumeCounter(counterId);
        return "Counter " + counterId + " resumed.";
    }
}

