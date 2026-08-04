package com.medcore.hms.doctor.slot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SlotNotFoundException extends RuntimeException {
    public SlotNotFoundException(UUID id) {
        super("Slot not found with id: " + id);
    }
    public SlotNotFoundException(String message) {
        super(message);
    }
}
