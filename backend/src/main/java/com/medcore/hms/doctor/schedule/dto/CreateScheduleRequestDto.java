package com.medcore.hms.doctor.schedule.dto;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;
import jakarta.validation.constraints.*;

import java.time.LocalTime;

public record CreateScheduleRequestDto(

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        LocalTime lunchBreakStart,

        LocalTime lunchBreakEnd,

        @Min(value = 5, message = "Slot duration must be at least 5 minutes")
        @Max(value = 120, message = "Slot duration must not exceed 120 minutes")
        Integer slotDurationMinutes
) {}
