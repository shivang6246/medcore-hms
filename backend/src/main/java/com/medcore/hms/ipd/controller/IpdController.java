package com.medcore.hms.ipd.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.AdmissionStatus;
import com.medcore.hms.ipd.service.IpdService;
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

@Tag(name = "Inpatient Admission & Discharge (IPD)", description = "REST APIs for IPD admissions, bed allocations, ward transfers, and discharge summaries.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class IpdController {

    private final IpdService ipdService;

    // ── Admission APIs ───────────────────────────────────────────────────────

    @Operation(summary = "Admit patient into IPD ward/bed")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Patient admitted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bed unavailable or active admission exists")
    })
    @PostMapping("/api/admissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> createAdmission(
            @Valid @RequestBody CreateAdmissionRequestDto dto) {
        log.info("REST request to admit patient ID: {} into bed ID: {}", dto.patientId(), dto.bedId());
        AdmissionResponseDto response = ipdService.createAdmission(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Patient admitted successfully"));
    }

    @Operation(summary = "Get admission details by ID")
    @GetMapping("/api/admissions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> getAdmissionById(
            @Parameter(description = "Admission UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch admission ID: {}", id);
        AdmissionResponseDto response = ipdService.getAdmissionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Admission details fetched successfully"));
    }

    @Operation(summary = "Transfer patient to new ward/room/bed")
    @PatchMapping("/api/admissions/{id}/transfer")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> transferBed(
            @Parameter(description = "Admission UUID", required = true) @PathVariable("id") UUID admissionId,
            @Valid @RequestBody TransferBedRequestDto dto) {
        log.info("REST request to transfer admission ID: {} to new bed ID: {}", admissionId, dto.newBedId());
        AdmissionResponseDto response = ipdService.transferBed(admissionId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient transferred successfully"));
    }

    @Operation(summary = "Discharge patient and generate summary")
    @PatchMapping("/api/admissions/{id}/discharge")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AdmissionResponseDto>> dischargePatient(
            @Parameter(description = "Admission UUID", required = true) @PathVariable("id") UUID admissionId,
            @Valid @RequestBody DischargeRequestDto dto) {
        log.info("REST request to discharge admission ID: {}", admissionId);
        AdmissionResponseDto response = ipdService.dischargePatient(admissionId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient discharged successfully"));
    }

    @Operation(summary = "Get available beds (paginated, optional ward filter)")
    @GetMapping("/api/beds/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<BedResponseDto>>> getAvailableBeds(
            @RequestParam(required = false) UUID wardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch available beds (ward filter: {})", wardId);
        PagedResponse<BedResponseDto> response = ipdService.getAvailableBeds(wardId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response, "Available beds fetched successfully"));
    }

    @Operation(summary = "Get all admissions (paginated, optional status filter)")
    @GetMapping("/api/admissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<AdmissionSummaryDto>>> getAllAdmissions(
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "admissionDate,desc") String sort) {
        log.info("REST request to fetch all admissions paginated (status: {})", status);
        PagedResponse<AdmissionSummaryDto> response = ipdService.getAllAdmissions(
                status, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Admissions fetched successfully"));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "admissionDate");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }
}
