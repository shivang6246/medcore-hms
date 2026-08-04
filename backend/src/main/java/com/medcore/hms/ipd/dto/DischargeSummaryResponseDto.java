package com.medcore.hms.ipd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for IPD Discharge Summary")
public record DischargeSummaryResponseDto(
        @Schema(description = "Discharge summary UUID")
        UUID id,

        @Schema(description = "Admission UUID")
        UUID admissionId,

        @Schema(description = "Attending Doctor name")
        String attendingDoctorName,

        @Schema(description = "Discharge timestamp")
        LocalDateTime dischargeDate,

        @Schema(description = "Final diagnosis")
        String finalDiagnosis,

        @Schema(description = "Treatment summary")
        String treatmentSummary,

        @Schema(description = "Discharge notes")
        String dischargeNotes,

        @Schema(description = "Follow-up instructions")
        String followUpInstructions,

        @Schema(description = "Final invoice UUID if generated")
        UUID finalInvoiceId
) {}
