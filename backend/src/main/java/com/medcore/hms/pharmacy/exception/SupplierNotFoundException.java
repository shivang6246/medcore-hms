package com.medcore.hms.pharmacy.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String message) {
        super(message);
    }

    public SupplierNotFoundException(UUID id) {
        super("Supplier not found with ID: " + id);
    }
}
