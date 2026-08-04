package com.medcore.hms.prescription.exception;

import java.util.UUID;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }

    public PrescriptionNotFoundException(UUID id) {
        super("Prescription not found with ID: " + id);
    }
}
