package com.example.bureaucratic_system_backend.model;

public class Membership {
    private String membershipNumber;
    private String citizenName;
    private String issueDate;
    private String citizenId;

    // Constructor
    public Membership(String membershipNumber, String citizenName, String issueDate, String citizenId) {
        this.membershipNumber = membershipNumber;
        this.citizenName = citizenName;
        this.issueDate = issueDate;
        this.citizenId = citizenId;
    }

    public Membership() {}

    // Getters and setters
    public String getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(String membershipId) {
        this.membershipNumber = membershipId;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public Object getCitizenName() {
        return citizenName;
    }

    public void setCitizenName(String citizenName){
        this.citizenName = citizenName;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public Object getIssueDate() {
        return issueDate;
    }
}
