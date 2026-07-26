package com.medcore.hms.hospital.exception;

public class HospitalAlreadyExistsException extends RuntimeException {
    public HospitalAlreadyExistsException(String message) {
        super(message);
    }
}
