package com.example.bureaucratic_system_backend.model;

import java.util.List;

public class Document {
    private String name;
    private List<String> dependencies;

    // Constructor
    public Document(String name, List<String> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public Document() {}

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
}
