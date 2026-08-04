package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class BedNotFoundException extends RuntimeException {
    public BedNotFoundException(String message) {
        super(message);
    }

    public BedNotFoundException(UUID id) {
        super("Bed not found with ID: " + id);
    }
}
