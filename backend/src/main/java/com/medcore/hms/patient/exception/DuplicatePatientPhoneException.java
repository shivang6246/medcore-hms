package com.medcore.hms.patient.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePatientPhoneException extends RuntimeException {
    public DuplicatePatientPhoneException(String phone) {
        super("Phone '" + phone + "' is already registered in this hospital");
    }
}
