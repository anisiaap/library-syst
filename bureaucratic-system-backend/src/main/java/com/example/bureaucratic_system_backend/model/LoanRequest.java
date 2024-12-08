package com.example.bureaucratic_system_backend.model;

public class LoanRequest {
    private String bookTitle;
    private String bookAuthor;
    private String citizenId;

    // Constructor
    public LoanRequest(String bookTitle, String bookAuthor, String citizenId) {
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.citizenId = citizenId;
    }

    public LoanRequest() {

    }

    // Getters and setters
    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }
}
