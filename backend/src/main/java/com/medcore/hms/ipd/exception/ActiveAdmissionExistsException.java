package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class ActiveAdmissionExistsException extends RuntimeException {
    public ActiveAdmissionExistsException(String message) {
        super(message);
    }

    public ActiveAdmissionExistsException(UUID patientId) {
        super("Patient with ID " + patientId + " already has an active IPD admission.");
    }
}
