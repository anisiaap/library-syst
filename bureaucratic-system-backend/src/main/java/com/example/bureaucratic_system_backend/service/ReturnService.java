package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Borrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReturnService {

    private static final Logger logger = LoggerFactory.getLogger(ReturnService.class);

    private final BorrowService borrowService;
    private final FeeService feeService;
    private final FirebaseService firebaseService;

    public ReturnService(BorrowService borrowService, FeeService feeService, FirebaseService firebaseService) {
        this.borrowService = borrowService;
        this.feeService = feeService;
        this.firebaseService = firebaseService;
    }

    public void processReturn(String membershipId, String bookTitle, String bookAuthor) {
        try {
            // Find the borrow record using membershipId, bookTitle, and bookAuthor
            Borrows borrow = firebaseService.getBorrowByMembershipAndBook(membershipId, bookTitle, bookAuthor);
            if (borrow == null) {
                throw new IllegalArgumentException("No active borrow record found for the provided details.");
            }

            // Update the return date
            String returnDate = LocalDate.now().toString();
            borrowService.updateReturnDate(borrow.getId(), returnDate);

            // Generate overdue fee if necessary
            feeService.generateOverdueFee(borrow.getId());

            logger.info("Book return processed successfully for membership ID: {}, book: '{}', author: '{}'",
                    membershipId, bookTitle, bookAuthor);
        } catch (Exception e) {
            logger.error("Error processing return: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}