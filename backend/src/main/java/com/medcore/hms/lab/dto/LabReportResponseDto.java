package com.medcore.hms.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for Lab Report")
public record LabReportResponseDto(
        @Schema(description = "Report UUID")
        UUID id,

        @Schema(description = "Associated Lab Test UUID")
        UUID labTestId,

        @Schema(description = "Test result findings")
        String result,

        @Schema(description = "Clinical remarks")
        String remarks,

        @Schema(description = "Report file URL")
        String reportFileUrl,

        @Schema(description = "Publication timestamp")
        LocalDateTime reportedAt,

        @Schema(description = "Technician/User full name who reported result")
        String reportedByName
) {}
