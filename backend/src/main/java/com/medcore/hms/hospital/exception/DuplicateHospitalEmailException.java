package com.medcore.hms.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateHospitalEmailException extends HospitalAlreadyExistsException {
    public DuplicateHospitalEmailException(String email) {
        super("A hospital with email '" + email + "' already exists.");
    }
}
