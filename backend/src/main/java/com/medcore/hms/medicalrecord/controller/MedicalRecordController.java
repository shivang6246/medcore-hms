package com.medcore.hms.medicalrecord.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.service.MedicalRecordService;
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

@Tag(name = "Medical Record Management", description = "REST APIs for managing patient medical records, clinical notes, diagnosis, and treatment plans.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    // ── Create ──────────────────────────────────────────────────────────────

    @Operation(summary = "Create a medical record", description = "Creates a new clinical medical record for a completed or ongoing appointment.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Medical record created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error or appointment mismatch"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient, doctor, or appointment not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate medical record for appointment")
    })
    @PostMapping("/api/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDto>> createMedicalRecord(
            @Valid @RequestBody CreateMedicalRecordRequestDto dto) {
        log.info("REST request to create medical record for appointment ID: {}", dto.appointmentId());
        MedicalRecordResponseDto response = medicalRecordService.createMedicalRecord(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medical record created successfully"));
    }

    // ── Get by ID ───────────────────────────────────────────────────────────

    @Operation(summary = "Get medical record by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found")
    })
    @GetMapping("/api/medical-records/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDto>> getMedicalRecordById(
            @Parameter(description = "Medical Record UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch medical record ID: {}", id);
        MedicalRecordResponseDto response = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical record fetched successfully"));
    }

    // ── List by Patient ──────────────────────────────────────────────────────

    @Operation(summary = "List medical records by Patient (paginated)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical records returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/api/patients/{patientId}/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicalRecordSummaryDto>>> getMedicalRecordsByPatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch medical records for patient ID: {}", patientId);
        PagedResponse<MedicalRecordSummaryDto> response = medicalRecordService.getMedicalRecordsByPatient(
                patientId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Patient medical records fetched successfully"));
    }

    // ── List by Doctor ───────────────────────────────────────────────────────

    @Operation(summary = "List medical records by Doctor (paginated)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical records returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/api/doctors/{doctorId}/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicalRecordSummaryDto>>> getMedicalRecordsByDoctor(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch medical records for doctor ID: {}", doctorId);
        PagedResponse<MedicalRecordSummaryDto> response = medicalRecordService.getMedicalRecordsByDoctor(
                doctorId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor medical records fetched successfully"));
    }

    // ── List All ─────────────────────────────────────────────────────────────

    @Operation(summary = "List all medical records (paginated)")
    @GetMapping("/api/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicalRecordSummaryDto>>> getAllMedicalRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("REST request to fetch all medical records paginated");
        PagedResponse<MedicalRecordSummaryDto> response = medicalRecordService.getAllMedicalRecords(
                PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "All medical records fetched successfully"));
    }

    // ── Update ──────────────────────────────────────────────────────────────

    @Operation(summary = "Update an existing medical record")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found")
    })
    @PutMapping("/api/medical-records/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDto>> updateMedicalRecord(
            @Parameter(description = "Medical Record UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicalRecordRequestDto dto) {
        log.info("REST request to update medical record ID: {}", id);
        MedicalRecordResponseDto response = medicalRecordService.updateMedicalRecord(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical record updated successfully"));
    }

    // ── Deactivate ───────────────────────────────────────────────────────────

    @Operation(summary = "Deactivate a medical record", description = "Soft deletes/deactivates a medical record by setting isActive = false.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Medical record deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Medical record not found")
    })
    @PatchMapping("/api/medical-records/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicalRecordResponseDto>> deactivateMedicalRecord(
            @Parameter(description = "Medical Record UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to deactivate medical record ID: {}", id);
        MedicalRecordResponseDto response = medicalRecordService.deactivateMedicalRecord(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medical record deactivated successfully"));
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
