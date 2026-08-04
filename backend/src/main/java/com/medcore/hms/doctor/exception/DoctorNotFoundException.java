package com.medcore.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(UUID id) {
        super("Doctor not found with id: " + id);
    }
    public DoctorNotFoundException(String message) {
        super(message);
    }
}
