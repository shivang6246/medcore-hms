package com.medcore.hms.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload to update an existing Prescription")
public record UpdatePrescriptionRequestDto(
        @NotBlank(message = "Medicine name is required")
        @Schema(description = "Updated medicine name", example = "Amoxicillin 250mg")
        String medicineName,

        @NotBlank(message = "Dosage is required")
        @Schema(description = "Updated dosage", example = "1 capsule")
        String dosage,

        @NotBlank(message = "Frequency is required")
        @Schema(description = "Updated frequency", example = "Twice daily")
        String frequency,

        @NotNull(message = "Duration is required")
        @Min(value = 1, message = "Duration must be at least 1 day")
        @Schema(description = "Updated duration in days", example = "5")
        Integer duration,

        @Schema(description = "Updated instructions", example = "Take before food")
        String instructions,

        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Updated total quantity", example = "10")
        Integer quantity
) {}
