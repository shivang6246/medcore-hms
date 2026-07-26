package com.medcore.hms.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRegistrationNumberException extends HospitalAlreadyExistsException {
    public DuplicateRegistrationNumberException(String number) {
        super("A hospital with registration number '" + number + "' already exists.");
    }
}
