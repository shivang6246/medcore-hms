package com.medcore.hms.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Executive Admin Dashboard statistics response DTO")
public record AdminDashboardDto(
        @Schema(description = "Total registered patients count")
        long totalPatients,

        @Schema(description = "Total active doctors count")
        long totalActiveDoctors,

        @Schema(description = "Today's scheduled appointments count")
        long todayAppointmentsCount,

        @Schema(description = "Monthly revenue collected")
        BigDecimal monthlyRevenue,

        @Schema(description = "Total outstanding invoice balance")
        BigDecimal totalOutstandingBalance,

        @Schema(description = "Bed occupancy rate percentage (0 - 100%)")
        double bedOccupancyRate,

        @Schema(description = "Total beds count")
        long totalBeds,

        @Schema(description = "Occupied beds count")
        long occupiedBeds,

        @Schema(description = "Male patient count")
        long malePatients,

        @Schema(description = "Female patient count")
        long femalePatients,

        @Schema(description = "Other / unspecified patient count")
        long otherPatients,

        @Schema(description = "Monthly revenue trends for charting")
        List<AnalyticsTrendDto> revenueTrends,

        @Schema(description = "Top departments by revenue")
        List<DepartmentRevenueDto> topDepartments,

        @Schema(description = "Top performing doctors")
        List<DoctorPerformanceDto> topDoctors
) {}
