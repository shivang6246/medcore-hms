package com.medcore.hms.lab.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.lab.dto.*;
import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.service.LabTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Laboratory Management", description = "REST APIs for lab test orders, sample collection tracking, status workflow, and publishing reports.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    // ── Order Lab Test ──────────────────────────────────────────────────────

    @Operation(summary = "Order a lab test", description = "Creates a new laboratory test order for a patient.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lab test ordered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient, doctor, or referenced entity not found")
    })
    @PostMapping("/api/lab-tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<LabTestResponseDto>> createLabTest(
            @Valid @RequestBody CreateLabTestRequestDto dto) {
        log.info("REST request to order lab test for patient ID: {}", dto.patientId());
        LabTestResponseDto response = labTestService.createLabTest(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lab test ordered successfully"));
    }

    // ── Get Lab Test by ID ──────────────────────────────────────────────────

    @Operation(summary = "Get lab test details by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lab test details returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lab test not found")
    })
    @GetMapping("/api/lab-tests/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN', 'PATIENT')")
    public ResponseEntity<ApiResponse<LabTestResponseDto>> getLabTestById(
            @Parameter(description = "Lab test UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch lab test ID: {}", id);
        LabTestResponseDto response = labTestService.getLabTestById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lab test fetched successfully"));
    }

    // ── List Lab Tests by Patient ───────────────────────────────────────────

    @Operation(summary = "List lab tests for a Patient (paginated)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient lab tests returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/api/patients/{id}/lab-tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<LabTestSummaryDto>>> getLabTestsByPatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable("id") UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch lab tests for patient ID: {}", patientId);
        PagedResponse<LabTestSummaryDto> response = labTestService.getLabTestsByPatient(
                patientId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Patient lab tests fetched successfully"));
    }

    // ── Update Status & Assign Technician ───────────────────────────────────

    @Operation(summary = "Update lab test status and technician", description = "Advances lab test status (e.g. REQUESTED -> SAMPLE_COLLECTED -> IN_PROGRESS).")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lab test not found")
    })
    @PatchMapping("/api/lab-tests/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabTestResponseDto>> updateLabTestStatus(
            @Parameter(description = "Lab test UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateLabTestStatusRequestDto dto) {
        log.info("REST request to update lab test status ID: {} to {}", id, dto.status());
        LabTestResponseDto response = labTestService.updateLabTestStatus(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Lab test status updated successfully"));
    }

    // ── Upload/Publish Lab Report ───────────────────────────────────────────

    @Operation(summary = "Publish lab report for lab test", description = "Uploads test findings and marks lab test status as COMPLETED.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Report published"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or invalid status"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lab test not found")
    })
    @PostMapping("/api/lab-tests/{id}/report")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<LabReportResponseDto>> publishLabReport(
            @Parameter(description = "Lab test UUID", required = true) @PathVariable("id") UUID labTestId,
            @Valid @RequestBody CreateLabReportRequestDto dto) {
        log.info("REST request to publish report for lab test ID: {}", labTestId);
        LabReportResponseDto response = labTestService.publishLabReport(labTestId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Lab report published successfully"));
    }

    // ── List All Lab Tests ───────────────────────────────────────────────────

    @Operation(summary = "List all lab tests (paginated, optional status filter)")
    @GetMapping("/api/lab-tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN')")
    public ResponseEntity<ApiResponse<PagedResponse<LabTestSummaryDto>>> getAllLabTests(
            @RequestParam(required = false) LabTestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch all lab tests paginated (status filter: {})", status);
        PagedResponse<LabTestSummaryDto> response = status != null
                ? labTestService.getLabTestsByStatus(status, PageRequest.of(page, size, parseSort(sort)))
                : labTestService.getAllLabTests(PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Lab tests fetched successfully"));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }
}
