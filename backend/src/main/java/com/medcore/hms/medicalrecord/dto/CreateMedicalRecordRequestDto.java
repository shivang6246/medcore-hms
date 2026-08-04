package com.medcore.hms.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request payload to create a new Medical Record")
public record CreateMedicalRecordRequestDto(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "UUID of the patient", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        @Schema(description = "UUID of the doctor", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID doctorId,

        @NotNull(message = "Appointment ID is required")
        @Schema(description = "UUID of the appointment", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID appointmentId,

        @NotBlank(message = "Symptoms must not be blank")
        @Schema(description = "Observed symptoms", example = "Severe headache, high fever, fatigue")
        String symptoms,

        @NotBlank(message = "Diagnosis must not be blank")
        @Schema(description = "Clinical diagnosis", example = "Acute Viral Fever")
        String diagnosis,

        @Schema(description = "Recommended treatment plan", example = "Paracetamol 500mg TDS for 5 days, rest and rehydration")
        String treatmentPlan,

        @Schema(description = "Additional clinical notes", example = "Patient advised to return if fever persists past 3 days")
        String notes,

        @FutureOrPresent(message = "Follow-up date must be today or in the future")
        @Schema(description = "Optional follow-up appointment date", example = "2026-08-15")
        LocalDate followUpDate
) {}
