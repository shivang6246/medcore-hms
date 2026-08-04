package com.medcore.hms.medicalrecord.exception;

import java.util.UUID;

public class MedicalRecordNotFoundException extends RuntimeException {
    public MedicalRecordNotFoundException(String message) {
        super(message);
    }

    public MedicalRecordNotFoundException(UUID id) {
        super("Medical record not found with ID: " + id);
    }
}
