package com.medcore.hms.ipd.exception;

import java.util.UUID;

public class BedNotAvailableException extends RuntimeException {
    public BedNotAvailableException(String message) {
        super(message);
    }

    public BedNotAvailableException(UUID bedId) {
        super("Bed with ID " + bedId + " is not available for admission.");
    }
}
