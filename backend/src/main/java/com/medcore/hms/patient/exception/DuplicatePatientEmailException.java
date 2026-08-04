package com.medcore.hms.patient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePatientEmailException extends RuntimeException {
    public DuplicatePatientEmailException(String email) {
        super("Email '" + email + "' is already registered");
    }
}
