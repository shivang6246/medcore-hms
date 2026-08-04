package com.medcore.hms.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Schema(description = "Request payload to update an existing Medical Record")
public record UpdateMedicalRecordRequestDto(
        @NotBlank(message = "Symptoms must not be blank")
        @Schema(description = "Updated symptoms", example = "Mild headache, fever subsided")
        String symptoms,

        @NotBlank(message = "Diagnosis must not be blank")
        @Schema(description = "Updated diagnosis", example = "Recovering Viral Fever")
        String diagnosis,

        @Schema(description = "Updated treatment plan", example = "Continue Paracetamol as needed, light diet")
        String treatmentPlan,

        @Schema(description = "Updated clinical notes", example = "Patient shows significant recovery")
        String notes,

        @FutureOrPresent(message = "Follow-up date must be today or in the future")
        @Schema(description = "Updated follow-up date", example = "2026-08-20")
        LocalDate followUpDate
) {}
