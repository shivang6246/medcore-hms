package com.medcore.hms.doctor.schedule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(UUID id) {
        super("Schedule not found with id: " + id);
    }
    public ScheduleNotFoundException(String message) {
        super(message);
    }
}
