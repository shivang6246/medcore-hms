package com.medcore.hms.appointment.exception;

import com.medcore.hms.appointment.entity.AppointmentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AppointmentStatusException extends RuntimeException {
    public AppointmentStatusException(AppointmentStatus current, AppointmentStatus target) {
        super("Invalid status transition from " + current + " to " + target);
    }
    public AppointmentStatusException(String message) {
        super(message);
    }
}
