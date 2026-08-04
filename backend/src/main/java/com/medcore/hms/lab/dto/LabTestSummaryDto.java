package com.medcore.hms.lab.dto;

import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.entity.TestPriority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary DTO for Lab Test listings")
public record LabTestSummaryDto(
        @Schema(description = "Lab test UUID")
        UUID id,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Doctor name")
        String doctorName,

        @Schema(description = "Test type")
        String testType,

        @Schema(description = "Priority")
        TestPriority priority,

        @Schema(description = "Status")
        LabTestStatus status,

        @Schema(description = "Order creation timestamp")
        LocalDateTime createdAt
) {}
