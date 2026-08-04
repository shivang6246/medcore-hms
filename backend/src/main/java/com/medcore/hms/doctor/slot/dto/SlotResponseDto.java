package com.medcore.hms.doctor.slot.dto;

import com.medcore.hms.doctor.slot.entity.SlotStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record SlotResponseDto(
        UUID id,
        UUID doctorId,
        UUID scheduleId,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        SlotStatus status,
        String blockedReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
