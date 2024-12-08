package com.example.bureaucratic_system_backend.model;

public interface Department {
    void pauseCounter(int counterId);
    void resumeCounter(int counterId);
}