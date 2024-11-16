package com.example.bureaucratic_system_backend.model;

public class Citizen {
    private String id;
    private String name;

    // Default constructor
    public Citizen() {
    }

    // Constructor
    public Citizen(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
