package com.example.bureaucratic_system_backend.model;

public class Fees {
    private String id;
    private String membershipId;
    private String amount;
    private String borrowId;
    private String paid;

    // Constructor
    public Fees(String id, String membershipId, String amount, String borrowId, String paid) {
        this.id = id;
        this.membershipId = membershipId;
        this.amount = amount;
        this.borrowId = borrowId;
        this.paid = paid;
    }

    // Default constructor
    public Fees() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(String borrowId) {
        this.borrowId = borrowId;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }
}