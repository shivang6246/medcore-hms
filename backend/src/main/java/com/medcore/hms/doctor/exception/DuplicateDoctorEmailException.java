package com.medcore.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDoctorEmailException extends DoctorAlreadyExistsException {
    public DuplicateDoctorEmailException(String email) {
        super("A doctor with email '" + email + "' already exists.");
    }
}
