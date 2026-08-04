package com.medcore.hms.pharmacy.exception;

import java.util.UUID;

public class MedicineNotFoundException extends RuntimeException {
    public MedicineNotFoundException(String message) {
        super(message);
    }

    public MedicineNotFoundException(UUID id) {
        super("Medicine not found with ID: " + id);
    }
}
