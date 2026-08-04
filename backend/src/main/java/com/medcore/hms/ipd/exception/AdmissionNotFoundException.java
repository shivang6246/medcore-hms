package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class AdmissionNotFoundException extends RuntimeException {
    public AdmissionNotFoundException(String message) {
        super(message);
    }

    public AdmissionNotFoundException(UUID id) {
        super("Admission record not found with ID: " + id);
    }
}
