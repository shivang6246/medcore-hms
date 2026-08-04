package com.medcore.hms.dashboard.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.dashboard.dto.*;
import com.medcore.hms.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Dashboard & Executive Analytics", description = "REST APIs for Admin, Doctor, and Receptionist executive dashboards and real-time operational analytics.")
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get Admin Executive Dashboard Statistics")
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard(
            @Parameter(description = "Optional hospital UUID filter") @RequestParam(required = false) UUID hospitalId) {
        log.info("REST request for Admin Dashboard stats (hospitalId: {})", hospitalId);
        AdminDashboardDto response = dashboardService.getAdminDashboard(hospitalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Admin dashboard statistics fetched successfully"));
    }

    @Operation(summary = "Get Doctor Clinical Dashboard Statistics")
    @GetMapping("/doctor")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<DoctorDashboardDto>> getDoctorDashboard(
            @Parameter(description = "Doctor UUID", required = true) @RequestParam UUID doctorId) {
        log.info("REST request for Doctor Dashboard stats (doctorId: {})", doctorId);
        DoctorDashboardDto response = dashboardService.getDoctorDashboard(doctorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor dashboard statistics fetched successfully"));
    }

    @Operation(summary = "Get Receptionist Operational Dashboard Statistics")
    @GetMapping("/reception")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<ReceptionDashboardDto>> getReceptionDashboard(
            @Parameter(description = "Optional hospital UUID filter") @RequestParam(required = false) UUID hospitalId) {
        log.info("REST request for Reception Dashboard stats (hospitalId: {})", hospitalId);
        ReceptionDashboardDto response = dashboardService.getReceptionDashboard(hospitalId);
        return ResponseEntity.ok(ApiResponse.success(response, "Reception dashboard statistics fetched successfully"));
    }

    @Operation(summary = "Get Monthly Revenue Analytics Trends")
    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<AnalyticsTrendDto>>> getRevenueAnalytics(
            @Parameter(description = "Optional hospital UUID filter") @RequestParam(required = false) UUID hospitalId,
            @RequestParam(defaultValue = "6") int months) {
        log.info("REST request for Revenue Analytics trends (months: {})", months);
        List<AnalyticsTrendDto> response = dashboardService.getRevenueAnalytics(hospitalId, months);
        return ResponseEntity.ok(ApiResponse.success(response, "Revenue analytics trends fetched successfully"));
    }

    @Operation(summary = "Get Appointment Volume Analytics Trends")
    @GetMapping("/analytics/appointments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<AnalyticsTrendDto>>> getAppointmentAnalytics(
            @Parameter(description = "Optional hospital UUID filter") @RequestParam(required = false) UUID hospitalId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("REST request for Appointment Analytics trends (days: {})", days);
        List<AnalyticsTrendDto> response = dashboardService.getAppointmentAnalytics(hospitalId, days);
        return ResponseEntity.ok(ApiResponse.success(response, "Appointment analytics trends fetched successfully"));
    }
}
