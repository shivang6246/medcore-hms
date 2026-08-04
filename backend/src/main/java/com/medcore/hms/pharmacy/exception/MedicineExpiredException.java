package com.medcore.hms.pharmacy.exception;

public class MedicineExpiredException extends RuntimeException {
    public MedicineExpiredException(String message) {
        super(message);
    }
}
