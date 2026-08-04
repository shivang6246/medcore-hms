package com.medcore.hms.ipd.dto;

import com.medcore.hms.ipd.entity.AdmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for IPD Admission")
public record AdmissionResponseDto(
        @Schema(description = "Admission UUID")
        UUID id,

        @Schema(description = "Admission reference number")
        String admissionNumber,

        @Schema(description = "Patient UUID")
        UUID patientId,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Attending Doctor UUID")
        UUID doctorId,

        @Schema(description = "Attending Doctor name")
        String doctorName,

        @Schema(description = "Ward name")
        String wardName,

        @Schema(description = "Room number")
        String roomNumber,

        @Schema(description = "Bed number")
        String bedNumber,

        @Schema(description = "Admission date")
        LocalDateTime admissionDate,

        @Schema(description = "Expected discharge date")
        LocalDateTime expectedDischargeDate,

        @Schema(description = "Discharge date")
        LocalDateTime dischargeDate,

        @Schema(description = "Reason / Diagnosis")
        String reason,

        @Schema(description = "Admission status")
        AdmissionStatus status,

        @Schema(description = "Discharge summary details if discharged")
        DischargeSummaryResponseDto dischargeSummary
) {}
