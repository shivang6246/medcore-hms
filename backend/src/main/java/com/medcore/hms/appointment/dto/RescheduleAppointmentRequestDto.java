package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RescheduleAppointmentRequestDto(

        @NotNull(message = "New slot ID is required")
        UUID newSlotId,

        @Size(max = 500)
        String reason
) {}
