package com.example.cabbooking.rider.dto;

public class AvailDto {
    private double distance;
    private boolean found;

    public AvailDto() {
    }

    public AvailDto(double distance, boolean found) {
        this.distance = distance;
        this.found = found;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
