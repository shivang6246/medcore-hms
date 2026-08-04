package com.medcore.hms.lab.exception;

import java.util.UUID;

public class LabTestNotFoundException extends RuntimeException {
    public LabTestNotFoundException(String message) {
        super(message);
    }

    public LabTestNotFoundException(UUID id) {
        super("Lab test not found with ID: " + id);
    }
}
