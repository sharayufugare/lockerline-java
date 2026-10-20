package com.lockerline.error;

public class NoSuchTower extends RuntimeException {
    public NoSuchTower() {
        super("Tower not found");
    }
}