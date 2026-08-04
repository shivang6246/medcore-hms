package com.medcore.hms.telemedicine.exception;

import java.util.UUID;

public class TelemedicineSessionNotFoundException extends RuntimeException {
    public TelemedicineSessionNotFoundException(String message) {
        super(message);
    }

    public TelemedicineSessionNotFoundException(UUID id) {
        super("Telemedicine session not found with ID: " + id);
    }
}
