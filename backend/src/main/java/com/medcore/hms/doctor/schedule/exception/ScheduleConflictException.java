package com.medcore.hms.doctor.schedule.exception;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {
    public ScheduleConflictException(UUID doctorId, DayOfWeek day) {
        super("Doctor " + doctorId + " already has an active schedule for " + day);
    }
    public ScheduleConflictException(String message) {
        super(message);
    }
}
