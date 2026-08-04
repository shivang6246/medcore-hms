package com.medcore.hms.ipd.dto;

import com.medcore.hms.ipd.entity.AdmissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary DTO for Admission listings")
public record AdmissionSummaryDto(
        @Schema(description = "Admission UUID")
        UUID id,

        @Schema(description = "Admission reference number")
        String admissionNumber,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Doctor name")
        String doctorName,

        @Schema(description = "Ward & Bed summary", example = "ICU-Ward / Room 101 / Bed B1")
        String bedLocation,

        @Schema(description = "Admission date")
        LocalDateTime admissionDate,

        @Schema(description = "Admission status")
        AdmissionStatus status
) {}
