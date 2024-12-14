package com.example.bureaucratic_system_backend.model;

public class Counter {
    private int counterId;
    private boolean isPaused;

    public Counter(int counterId, boolean isPaused) {
        this.counterId = counterId;
        this.isPaused = isPaused;
    }

    public int getCounterId() {
        return counterId;
    }

    public void setCounterId(int counterId) {
        this.counterId = counterId;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    @Override
    public String toString() {
        return "Counter{" +
                "id=" + counterId +
                ", paused=" + isPaused +
                '}';
    }
}
