package com.medcore.hms.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Department revenue metrics DTO")
public record DepartmentRevenueDto(
        @Schema(description = "Department UUID")
        UUID departmentId,

        @Schema(description = "Department name")
        String departmentName,

        @Schema(description = "Total revenue generated")
        BigDecimal totalRevenue,

        @Schema(description = "Total consultations count")
        long consultationCount
) {}
