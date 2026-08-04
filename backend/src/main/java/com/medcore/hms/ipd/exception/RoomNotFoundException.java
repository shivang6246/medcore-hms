package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(String message) {
        super(message);
    }

    public RoomNotFoundException(UUID id) {
        super("Room not found with ID: " + id);
    }
}
