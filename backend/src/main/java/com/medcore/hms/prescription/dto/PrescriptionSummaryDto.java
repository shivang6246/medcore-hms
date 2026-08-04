package com.medcore.hms.prescription.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Summary representation of a Prescription for listing views")
public record PrescriptionSummaryDto(
        @Schema(description = "Prescription UUID")
        UUID id,

        @Schema(description = "Medicine name")
        String medicineName,

        @Schema(description = "Dosage")
        String dosage,

        @Schema(description = "Frequency")
        String frequency,

        @Schema(description = "Duration in days")
        Integer duration,

        @Schema(description = "Active status")
        boolean isActive
) {}
