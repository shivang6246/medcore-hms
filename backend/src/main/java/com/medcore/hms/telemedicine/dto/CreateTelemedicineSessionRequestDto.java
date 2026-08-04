package com.medcore.hms.telemedicine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request payload to create a new video consultation telemedicine room session")
public record CreateTelemedicineSessionRequestDto(
        @NotNull(message = "Appointment ID is required")
        @Schema(description = "Appointment UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID appointmentId,

        @NotNull(message = "Scheduled start time is required")
        @Schema(description = "Scheduled video consultation start timestamp")
        LocalDateTime scheduledStartTime
) {}
