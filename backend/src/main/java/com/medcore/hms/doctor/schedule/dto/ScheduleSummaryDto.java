package com.medcore.hms.doctor.schedule.dto;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;

import java.time.LocalTime;
import java.util.UUID;

public record ScheduleSummaryDto(
        UUID id,
        UUID doctorId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        int slotDurationMinutes,
        boolean isActive
) {}
