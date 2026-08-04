package com.medcore.hms.prescription.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequestDto;
import com.medcore.hms.prescription.dto.PrescriptionResponseDto;
import com.medcore.hms.prescription.dto.PrescriptionSummaryDto;
import com.medcore.hms.prescription.dto.UpdatePrescriptionRequestDto;
import com.medcore.hms.prescription.service.PrescriptionService;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Prescription Management", description = "REST APIs for managing medicine prescriptions linked to medical records.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ── Create ──────────────────────────────────────────────────────────────

    @Operation(summary = "Create a prescription", description = "Adds a new prescribed medicine to a medical record.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Prescription created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found")
    })
    @PostMapping("/api/prescriptions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> createPrescription(
            @Valid @RequestBody CreatePrescriptionRequestDto dto) {
        log.info("REST request to create prescription for medical record ID: {}", dto.medicalRecordId());
        PrescriptionResponseDto response = prescriptionService.createPrescription(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prescription created successfully"));
    }

    // ── Get by ID ───────────────────────────────────────────────────────────

    @Operation(summary = "Get prescription by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    })
    @GetMapping("/api/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescriptionById(
            @Parameter(description = "Prescription UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch prescription ID: {}", id);
        PrescriptionResponseDto response = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription fetched successfully"));
    }

    // ── List by Medical Record ───────────────────────────────────────────────

    @Operation(summary = "List prescriptions by Medical Record (paginated)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescriptions returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found")
    })
    @GetMapping("/api/medical-records/{id}/prescriptions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<PrescriptionSummaryDto>>> getPrescriptionsByMedicalRecord(
            @Parameter(description = "Medical Record UUID", required = true) @PathVariable("id") UUID medicalRecordId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch prescriptions for medical record ID: {}", medicalRecordId);
        PagedResponse<PrescriptionSummaryDto> response = prescriptionService.getPrescriptionsByMedicalRecord(
                medicalRecordId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Prescriptions fetched successfully"));
    }

    @Operation(summary = "Get list of all prescriptions for a Medical Record")
    @GetMapping("/api/medical-records/{id}/prescriptions/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getPrescriptionListByMedicalRecord(
            @Parameter(description = "Medical Record UUID", required = true) @PathVariable("id") UUID medicalRecordId) {
        log.info("REST request to fetch full prescription list for medical record ID: {}", medicalRecordId);
        List<PrescriptionResponseDto> list = prescriptionService.getPrescriptionListByMedicalRecord(medicalRecordId);
        return ResponseEntity.ok(ApiResponse.success(list, "Prescription list fetched successfully"));
    }

    // ── Update ──────────────────────────────────────────────────────────────

    @Operation(summary = "Update an existing prescription")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    })
    @PutMapping("/api/prescriptions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> updatePrescription(
            @Parameter(description = "Prescription UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdatePrescriptionRequestDto dto) {
        log.info("REST request to update prescription ID: {}", id);
        PrescriptionResponseDto response = prescriptionService.updatePrescription(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription updated successfully"));
    }

    // ── Deactivate ───────────────────────────────────────────────────────────

    @Operation(summary = "Deactivate a prescription", description = "Soft deletes/deactivates a prescription by setting isActive = false.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    })
    @PatchMapping("/api/prescriptions/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> deactivatePrescription(
            @Parameter(description = "Prescription UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to deactivate prescription ID: {}", id);
        PrescriptionResponseDto response = prescriptionService.deactivatePrescription(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Prescription deactivated successfully"));
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
