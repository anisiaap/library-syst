package com.example.bureaucratic_system_backend.model;

public class Membership {
    private String id;
    private String issueDate;
    private String citizenId;

    // Constructor
    public Membership(String membershipNumber , String issueDate, String citizenId) {
        this.id = membershipNumber;
        this.issueDate = issueDate;
        this.citizenId = citizenId;
    }

    public Membership() {}

    // Getters and setters
    public String getMembershipNumber() {
        return id;
    }

    public void setMembershipNumber(String membershipId) {
        this.id = membershipId;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }


    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public Object getIssueDate() {
        return issueDate;
    }
}
