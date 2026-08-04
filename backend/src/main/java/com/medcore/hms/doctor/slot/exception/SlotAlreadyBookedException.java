package com.medcore.hms.doctor.slot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(UUID slotId) {
        super("Slot " + slotId + " is already booked and cannot be modified");
    }
}
