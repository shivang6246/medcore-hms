package com.medcore.hms.billing.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(String message) {
        super(message);
    }

    public PaymentNotFoundException(UUID id) {
        super("Payment transaction not found with ID: " + id);
    }
}
