package com.medcore.hms.hospital.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class HospitalNotFoundException extends RuntimeException {
    public HospitalNotFoundException(UUID id) {
        super("Hospital not found with id: " + id);
    }
}
