package com.medcore.hms.doctor.slot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SlotGenerationException extends RuntimeException {
    public SlotGenerationException(String message) {
        super(message);
    }
}
