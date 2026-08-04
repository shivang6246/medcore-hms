package com.medcore.hms.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary representation of a Medical Record for listing endpoints")
public record MedicalRecordSummaryDto(
        @Schema(description = "Medical Record UUID")
        UUID id,

        @Schema(description = "Patient full name")
        String patientName,

        @Schema(description = "Doctor full name")
        String doctorName,

        @Schema(description = "Appointment reference number")
        String appointmentNumber,

        @Schema(description = "Diagnosis summary")
        String diagnosis,

        @Schema(description = "Follow-up date")
        LocalDate followUpDate,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Creation date")
        LocalDateTime createdAt
) {}
