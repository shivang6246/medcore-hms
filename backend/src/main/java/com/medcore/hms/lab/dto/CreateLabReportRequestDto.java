package com.medcore.hms.lab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Request payload to create/publish a lab report")
public record CreateLabReportRequestDto(
        @NotBlank(message = "Result content is required")
        @Schema(description = "Detailed test findings/results", example = "Hemoglobin: 14.5 g/dL, WBC: 6,500 /mcL, Platelets: 250,000 /mcL. All values within normal range.")
        String result,

        @Schema(description = "Technician remarks", example = "Sample processed without hemolysis.")
        String remarks,

        @Schema(description = "URL to PDF or attachment of lab report", example = "https://medcore-storage.com/reports/lab-cbc-1002.pdf")
        String reportFileUrl,

        @Schema(description = "ID of the reporting user/technician")
        UUID reportedById
) {}
