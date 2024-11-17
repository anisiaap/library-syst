package com.example.bureaucratic_system_backend.model;


public class Admin {
    private String id;

    // Default constructor
    public Admin() {
    }

    // Constructor
    public Admin(String id) {
        this.id = id;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}