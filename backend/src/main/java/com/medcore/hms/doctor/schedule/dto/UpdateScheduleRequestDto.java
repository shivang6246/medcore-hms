package com.medcore.hms.doctor.schedule.dto;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalTime;

public record UpdateScheduleRequestDto(

        DayOfWeek dayOfWeek,

        LocalTime startTime,

        LocalTime endTime,

        LocalTime lunchBreakStart,

        LocalTime lunchBreakEnd,

        @Min(value = 5, message = "Slot duration must be at least 5 minutes")
        @Max(value = 120, message = "Slot duration must not exceed 120 minutes")
        Integer slotDurationMinutes
) {}
