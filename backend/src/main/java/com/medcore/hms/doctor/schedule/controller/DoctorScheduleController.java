package com.medcore.hms.doctor.schedule.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.doctor.schedule.dto.*;
import com.medcore.hms.doctor.schedule.service.DoctorScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Doctor Schedule", description = "REST APIs for managing doctor weekly working schedules.")
@Slf4j
@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    @Operation(summary = "Create a schedule", description = "Defines working hours for a specific day of the week. Duplicate days are rejected (409).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Schedule created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Schedule already exists for that day")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> createSchedule(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody CreateScheduleRequestDto dto) {
        log.info("Creating schedule for doctor {} on {}", doctorId, dto.dayOfWeek());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(scheduleService.createSchedule(doctorId, dto), "Schedule created successfully"));
    }

    @Operation(summary = "List all schedules for a doctor")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedules returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<ScheduleSummaryDto>>> getAllSchedules(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId) {
        log.debug("Fetching schedules for doctor {}", doctorId);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAllSchedulesByDoctor(doctorId), "Schedules fetched successfully"));
    }

    @Operation(summary = "Get a schedule by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> getScheduleById(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Schedule UUID", required = true) @PathVariable UUID scheduleId) {
        log.debug("Fetching schedule {} for doctor {}", scheduleId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleById(doctorId, scheduleId), "Schedule fetched successfully"));
    }

    @Operation(summary = "Update a schedule", description = "Partially updates schedule fields. Null fields are ignored.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Schedule not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Day conflict")
    })
    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponseDto>> updateSchedule(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Schedule UUID", required = true) @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateScheduleRequestDto dto) {
        log.info("Updating schedule {} for doctor {}", scheduleId, doctorId);
        return ResponseEntity.ok(ApiResponse.success(scheduleService.updateSchedule(doctorId, scheduleId, dto), "Schedule updated successfully"));
    }

    @Operation(summary = "Delete a schedule")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Schedule deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Schedule not found")
    })
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Schedule UUID", required = true) @PathVariable UUID scheduleId) {
        log.info("Deleting schedule {} for doctor {}", scheduleId, doctorId);
        scheduleService.deleteSchedule(doctorId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deleted successfully"));
    }

    @Operation(summary = "Activate a schedule")
    @PatchMapping("/{scheduleId}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateSchedule(
            @PathVariable UUID doctorId,
            @PathVariable UUID scheduleId) {
        log.info("Activating schedule {} for doctor {}", scheduleId, doctorId);
        scheduleService.activateSchedule(doctorId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule activated successfully"));
    }

    @Operation(summary = "Deactivate a schedule")
    @PatchMapping("/{scheduleId}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateSchedule(
            @PathVariable UUID doctorId,
            @PathVariable UUID scheduleId) {
        log.info("Deactivating schedule {} for doctor {}", scheduleId, doctorId);
        scheduleService.deactivateSchedule(doctorId, scheduleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Schedule deactivated successfully"));
    }
}
