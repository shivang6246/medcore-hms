package com.medcore.hms.lab.dto;

import com.medcore.hms.lab.entity.LabTestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload to update status and technician assignment for a lab test")
public record UpdateLabTestStatusRequestDto(
        @NotNull(message = "Status is required")
        @Schema(description = "Target status", example = "SAMPLE_COLLECTED")
        LabTestStatus status,

        @Schema(description = "Optional technician user ID to assign", example = "e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b")
        UUID technicianId
) {}
