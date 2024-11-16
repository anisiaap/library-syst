package com.example.bureaucratic_system_backend.model;

import java.util.List;

public class Office {
    private String name;
    private int counterCount;
    private List<Document> documents;

    // Constructor
    public Office(String name, int counterCount, List<Document> documents) {
        this.name = name;
        this.counterCount = counterCount;
        this.documents = documents;
    }

    public Office() {}

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCounterCount() {
        return counterCount;
    }

    public void setCounterCount(int counterCount) {
        this.counterCount = counterCount;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
