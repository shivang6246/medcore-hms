package com.medcore.hms.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for a Medical Record")
public record MedicalRecordResponseDto(
        @Schema(description = "Medical record UUID")
        UUID id,

        @Schema(description = "Associated patient details")
        PatientRefDto patient,

        @Schema(description = "Associated doctor details")
        DoctorRefDto doctor,

        @Schema(description = "Associated appointment details")
        AppointmentRefDto appointment,

        @Schema(description = "Symptoms recorded")
        String symptoms,

        @Schema(description = "Diagnosis recorded")
        String diagnosis,

        @Schema(description = "Treatment plan")
        String treatmentPlan,

        @Schema(description = "Clinical notes")
        String notes,

        @Schema(description = "Follow-up date")
        LocalDate followUpDate,

        @Schema(description = "Record active status")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
    public record PatientRefDto(
            UUID id,
            String patientId,
            String firstName,
            String lastName,
            String phone
    ) {}

    public record DoctorRefDto(
            UUID id,
            String firstName,
            String lastName,
            String specialization
    ) {}

    public record AppointmentRefDto(
            UUID id,
            String appointmentNumber,
            LocalDate appointmentDate
    ) {}
}
