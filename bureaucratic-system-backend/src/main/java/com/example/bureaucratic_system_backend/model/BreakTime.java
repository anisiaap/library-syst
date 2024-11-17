package com.example.bureaucratic_system_backend.model;

public class BreakTime {
    private String department;
    private int counterId;

    // Default constructor
    public BreakTime() {}

    // Constructor
    public BreakTime(String department, int counterId) {
        this.department = department;
        this.counterId = counterId;
    }

    // Getters and Setters
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }

    @Override
    public String toString() {
        return "BreakTime{" +
                "department='" + department + '\'' +
                ", counterId=" + counterId +
                '}';
    }
}
