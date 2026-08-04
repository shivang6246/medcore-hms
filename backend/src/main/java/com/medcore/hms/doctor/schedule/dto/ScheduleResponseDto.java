package com.medcore.hms.doctor.schedule.dto;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleResponseDto(
        UUID id,
        UUID doctorId,
        String doctorName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        LocalTime lunchBreakStart,
        LocalTime lunchBreakEnd,
        int slotDurationMinutes,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
