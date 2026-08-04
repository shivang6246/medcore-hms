package com.medcore.hms.lab.dto;

import com.medcore.hms.lab.entity.TestPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload to order a new laboratory test")
public record CreateLabTestRequestDto(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "UUID of the patient", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        @Schema(description = "UUID of the ordering doctor", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID doctorId,

        @Schema(description = "Optional appointment UUID", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID appointmentId,

        @Schema(description = "Optional medical record UUID", example = "d4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a")
        UUID medicalRecordId,

        @NotBlank(message = "Test type is required")
        @Schema(description = "Name/type of lab test ordered", example = "Complete Blood Count (CBC)")
        String testType,

        @Schema(description = "Test priority", example = "URGENT")
        TestPriority priority,

        @Schema(description = "Special instructions for laboratory staff", example = "Fasting required for 12 hours prior")
        String instructions
) {}
