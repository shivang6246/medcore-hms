package com.medcore.hms.appointment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class SlotNotAvailableException extends RuntimeException {
    public SlotNotAvailableException(UUID slotId) {
        super("Slot is not available for booking: " + slotId);
    }
    public SlotNotAvailableException(String message) {
        super(message);
    }
}
