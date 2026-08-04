package com.medcore.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLicenseNumberException extends DoctorAlreadyExistsException {
    public DuplicateLicenseNumberException(String licenseNumber) {
        super("A doctor with license number '" + licenseNumber + "' already exists.");
    }
}
