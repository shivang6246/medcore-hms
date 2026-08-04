package com.medcore.hms.medicalrecord.exception;

import java.util.UUID;

public class DuplicateMedicalRecordException extends RuntimeException {
    public DuplicateMedicalRecordException(String message) {
        super(message);
    }

    public DuplicateMedicalRecordException(UUID appointmentId) {
        super("A medical record already exists for appointment ID: " + appointmentId);
    }
}
