package com.medcore.hms.doctor.slot.dto;

import com.medcore.hms.doctor.slot.entity.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SlotSummaryDto(
        UUID id,
        LocalDate slotDate,
        LocalTime startTime,
        LocalTime endTime,
        SlotStatus status
) {}
