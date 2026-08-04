package com.medcore.hms.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Doctor performance metrics DTO")
public record DoctorPerformanceDto(
        @Schema(description = "Doctor UUID")
        UUID doctorId,

        @Schema(description = "Doctor full name")
        String doctorName,

        @Schema(description = "Specialization")
        String specialization,

        @Schema(description = "Completed appointment count")
        long completedAppointments,

        @Schema(description = "Revenue generated")
        BigDecimal totalRevenue
) {}
