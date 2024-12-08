package com.example.bureaucratic_system_backend.model;


public class ReturnRequest {
    private String membershipId;
    private String bookTitle;
    private String bookAuthor;

    // Constructors
    public ReturnRequest(String membershipId, String bookTitle, String bookAuthor) {
        this.membershipId = membershipId;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
    }

    public ReturnRequest() {}

    // Getters and setters
    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

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
}