package com.lockerline.error;

public class NoSuchDoor extends RuntimeException {
    public NoSuchDoor() {
        super("Door not found");
    }
}