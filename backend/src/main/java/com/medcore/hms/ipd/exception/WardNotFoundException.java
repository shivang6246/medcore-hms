package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class WardNotFoundException extends RuntimeException {
    public WardNotFoundException(String message) {
        super(message);
    }

    public WardNotFoundException(UUID id) {
        super("Ward not found with ID: " + id);
    }
}
