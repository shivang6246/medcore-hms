package com.medcore.hms.medicalrecord.exception;

public class AppointmentMismatchException extends RuntimeException {
    public AppointmentMismatchException(String message) {
        super(message);
    }
}
