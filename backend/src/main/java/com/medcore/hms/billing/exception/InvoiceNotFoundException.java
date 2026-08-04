package com.medcore.hms.billing.exception;

import java.util.UUID;

public class InvoiceNotFoundException extends RuntimeException {
    public InvoiceNotFoundException(String message) {
        super(message);
    }

    public InvoiceNotFoundException(UUID id) {
        super("Invoice not found with ID: " + id);
    }
}
