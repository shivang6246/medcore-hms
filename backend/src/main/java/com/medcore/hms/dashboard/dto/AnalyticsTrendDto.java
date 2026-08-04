package com.medcore.hms.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Generic label/value trend metric for analytics charting")
public record AnalyticsTrendDto(
        @Schema(description = "Period label (e.g. '2026-08', 'Aug 04', 'Monday')")
        String label,

        @Schema(description = "Count or total metric")
        BigDecimal value,

        @Schema(description = "Secondary count metric if applicable")
        Long count
) {}
