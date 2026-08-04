package com.medcore.hms.ipd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Request payload to discharge IPD patient and generate discharge summary")
public record DischargeRequestDto(
        @NotNull(message = "Discharge date is required")
        @Schema(description = "Discharge timestamp")
        LocalDateTime dischargeDate,

        @NotBlank(message = "Final diagnosis is required")
        @Schema(description = "Final diagnosis description", example = "Post-appendectomy recovery completed")
        String finalDiagnosis,

        @Schema(description = "Treatment summary", example = "Surgical excision performed, IV antibiotics completed")
        String treatmentSummary,

        @Schema(description = "Discharge notes", example = "Patient stable, vitals normal")
        String dischargeNotes,

        @Schema(description = "Follow-up instructions", example = "Suture removal in 7 days, take prescribed meds")
        String followUpInstructions,

        @Schema(description = "Attending doctor UUID if different from admission doctor")
        UUID attendingDoctorId
) {}
