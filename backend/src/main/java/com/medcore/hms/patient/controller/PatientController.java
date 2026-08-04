package com.medcore.hms.patient.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.patient.dto.*;
import com.medcore.hms.patient.entity.BloodGroup;
import com.medcore.hms.patient.service.PatientService;
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

@Tag(name = "Patient Management", description = "REST APIs for patient registration, profile management, medical records, emergency contacts, insurance and lifecycle control.")
@Slf4j
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @Operation(
            summary = "Register a new patient",
            description = "Creates a patient profile linked to a hospital. Auto-generates a hospital-scoped patient ID. " +
                    "Validates unique phone (per hospital) and unique email (global). Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Patient registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found or inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate phone or email"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> createPatient(
            @Valid @RequestBody CreatePatientRequestDto dto) {
        log.info("Received request to register patient — hospital: {}", dto.hospitalId());
        PatientResponseDto created = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Patient registered successfully"));
    }

    @Operation(
            summary = "List patients (paginated)",
            description = "Returns a paginated list of all patients for a hospital. Supports page/size/sort parameters. " +
                    "Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR, and NURSE."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patients returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<PagedResponse<PatientSummaryDto>>> getAllPatients(
            @Parameter(description = "Hospital UUID to scope results") @RequestParam UUID hospitalId,
            @Parameter(description = "Page index (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction, e.g. 'lastName,asc'") @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.debug("Listing patients — hospital: {}, page: {}, size: {}", hospitalId, page, size);
        PagedResponse<PatientSummaryDto> result = patientService.getAllPatients(
                hospitalId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(result, "Patients fetched successfully"));
    }

    @Operation(
            summary = "Search and filter patients",
            description = "Multi-criteria search by name, phone, email, patientId, blood group and active status. " +
                    "All parameters are optional and combinable."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<ApiResponse<PagedResponse<PatientSummaryDto>>> searchPatients(
            @Parameter(description = "Filter by hospital UUID") @RequestParam(required = false) UUID hospitalId,
            @Parameter(description = "Search by name (first, last, or full)") @RequestParam(required = false) String name,
            @Parameter(description = "Search by phone") @RequestParam(required = false) String phone,
            @Parameter(description = "Search by email") @RequestParam(required = false) String email,
            @Parameter(description = "Search by patient ID") @RequestParam(required = false) String patientId,
            @Parameter(description = "Filter by blood group") @RequestParam(required = false) BloodGroup bloodGroup,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort) {
        log.debug("Searching patients — criteria: name={}, phone={}, bloodGroup={}", name, phone, bloodGroup);
        PatientSearchCriteria criteria = new PatientSearchCriteria(hospitalId, name, phone, email, patientId, bloodGroup, isActive);
        PagedResponse<PatientSummaryDto> result = patientService.searchPatients(
                criteria, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(result, "Search completed successfully"));
    }

    @Operation(
            summary = "Get patient by UUID",
            description = "Returns full patient profile by UUID. Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR, NURSE, and PATIENT (own profile)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> getPatientById(
            @Parameter(description = "Patient UUID", required = true) @PathVariable UUID id) {
        log.debug("Fetching patient — ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientById(id), "Patient fetched successfully"));
    }

    @Operation(
            summary = "Get patient by hospital-scoped patient ID",
            description = "Lookup by the human-readable patientId (e.g. P-2026-00001). Requires hospital context."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @GetMapping("/by-patient-id/{patientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> getPatientByPatientId(
            @Parameter(description = "Hospital-scoped patient ID, e.g. P-2026-00001", required = true) @PathVariable String patientId,
            @Parameter(description = "Hospital UUID for scoping", required = true) @RequestParam UUID hospitalId) {
        log.debug("Fetching patient by patientId: {} in hospital: {}", patientId, hospitalId);
        return ResponseEntity.ok(ApiResponse.success(
                patientService.getPatientByPatientId(patientId, hospitalId), "Patient fetched successfully"));
    }

    @Operation(
            summary = "Update patient profile",
            description = "Partial update — null fields are ignored. Validates phone/email uniqueness on change. " +
                    "Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, and NURSE (clinical fields only)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate phone or email")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'NURSE')")
    public ResponseEntity<ApiResponse<PatientResponseDto>> updatePatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequestDto dto) {
        log.info("Updating patient — ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(patientService.updatePatient(id, dto), "Patient updated successfully"));
    }

    @Operation(summary = "Activate a patient", description = "Sets isActive = true. Accessible by SUPER_ADMIN and HOSPITAL_ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activatePatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable UUID id) {
        log.info("Activating patient — ID: {}", id);
        patientService.activatePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient activated successfully"));
    }

    @Operation(summary = "Deactivate a patient", description = "Soft-deactivates (isActive = false). Accessible by SUPER_ADMIN and HOSPITAL_ADMIN.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Patient deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivatePatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable UUID id) {
        log.info("Deactivating patient — ID: {}", id);
        patientService.deactivatePatient(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient deactivated successfully"));
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }
}
