package com.medcore.hms.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload to create a new Prescription")
public record CreatePrescriptionRequestDto(
        @NotNull(message = "Medical Record ID is required")
        @Schema(description = "UUID of the associated medical record", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID medicalRecordId,

        @NotBlank(message = "Medicine name is required")
        @Schema(description = "Name of the prescribed medicine", example = "Amoxicillin 500mg")
        String medicineName,

        @NotBlank(message = "Dosage is required")
        @Schema(description = "Dosage details", example = "1 capsule")
        String dosage,

        @NotBlank(message = "Frequency is required")
        @Schema(description = "Frequency of administration", example = "3 times daily after meals")
        String frequency,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        @Schema(description = "Duration in days", example = "7")
        Integer duration,

        @Schema(description = "Additional instructions for patient", example = "Complete full course, drink plenty of water")
        String instructions,

        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Total quantity of medication unit", example = "21")
        Integer quantity
) {}
