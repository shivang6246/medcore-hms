package com.medcore.hms.ipd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request payload to admit a patient into IPD ward/bed")
public record CreateAdmissionRequestDto(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "Patient UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        @Schema(description = "Attending Doctor UUID", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID doctorId,

        @NotNull(message = "Ward ID is required")
        @Schema(description = "Ward UUID", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID wardId,

        @NotNull(message = "Room ID is required")
        @Schema(description = "Room UUID", example = "d4e5f6a7-b89c-0d1e-2f3a-4b5c6d7e8f9a")
        UUID roomId,

        @NotNull(message = "Bed ID is required")
        @Schema(description = "Bed UUID", example = "e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b")
        UUID bedId,

        @NotNull(message = "Admission date is required")
        @Schema(description = "Admission timestamp")
        LocalDateTime admissionDate,

        @Schema(description = "Expected discharge timestamp")
        LocalDateTime expectedDischargeDate,

        @Schema(description = "Admission reason / diagnosis", example = "Acute appendicitis observation")
        String reason
) {}
