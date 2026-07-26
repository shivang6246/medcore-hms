package com.medcore.hms.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLicenseNumberException extends HospitalAlreadyExistsException {
    public DuplicateLicenseNumberException(String licenseNumber) {
        super("Hospital with license number '" + licenseNumber + "' already exists.");
    }
}
