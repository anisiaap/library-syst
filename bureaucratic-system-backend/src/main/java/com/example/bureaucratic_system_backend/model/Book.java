package com.example.bureaucratic_system_backend.model;

public class Book {
    private String id;
    private String name;
    private String author;
    private boolean available;


    // Default constructor for Firebase deserialization
    public Book() {
    }

    // Constructor
    public Book(String id, String name, String author, boolean available) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.available = available;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

}
