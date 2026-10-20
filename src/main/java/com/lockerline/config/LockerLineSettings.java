package com.lockerline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lockerline")
public class LockerLineSettings {

    private int totalDoors = 48;
    private int smallDoors = 20;
    private int mediumDoors = 20;
    private int largeDoors = 8;

    // Default Constructor
    public LockerLineSettings() {
    }

    // Getters & Setters
    public int getTotalDoors() {
        return totalDoors;
    }

    public void setTotalDoors(int totalDoors) {
        this.totalDoors = totalDoors;
    }

    public int getSmallDoors() {
        return smallDoors;
    }

    public void setSmallDoors(int smallDoors) {
        this.smallDoors = smallDoors;
    }

    public int getMediumDoors() {
        return mediumDoors;
    }

    public void setMediumDoors(int mediumDoors) {
        this.mediumDoors = mediumDoors;
    }

    public int getLargeDoors() {
        return largeDoors;
    }

    public void setLargeDoors(int largeDoors) {
        this.largeDoors = largeDoors;
    }
}