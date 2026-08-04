package com.medcore.hms.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for a Prescription")
public record PrescriptionResponseDto(
        @Schema(description = "Prescription UUID")
        UUID id,

        @Schema(description = "Associated Medical Record UUID")
        UUID medicalRecordId,

        @Schema(description = "Medicine name")
        String medicineName,

        @Schema(description = "Dosage information")
        String dosage,

        @Schema(description = "Frequency of medicine")
        String frequency,

        @Schema(description = "Duration in days")
        Integer duration,

        @Schema(description = "Special instructions")
        String instructions,

        @Schema(description = "Quantity prescribed")
        Integer quantity,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
