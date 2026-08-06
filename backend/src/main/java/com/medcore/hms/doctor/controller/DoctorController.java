package com.medcore.hms.doctor.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.dto.*;
import com.medcore.hms.doctor.service.DoctorService;
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

/**
 * REST controller for Doctor lifecycle management.
 *
 * <ul>
 *   <li>SUPER_ADMIN — full access</li>
 *   <li>HOSPITAL_ADMIN — manage doctors within their hospital</li>
 *   <li>DOCTOR — view and update own profile / availability</li>
 * </ul>
 */
@Tag(name = "Doctor Management", description = "REST APIs for doctor onboarding, profile management, department assignment, availability and lifecycle control.")
@Slf4j
@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // -------------------------------------------------------------------------
    // POST /api/doctors — Onboard a new doctor
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Onboard a new doctor",
            description = "Creates a User account and Doctor profile in one step. Validates hospital/department membership, " +
                    "license uniqueness, and employee ID uniqueness within the hospital. Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Doctor onboarded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital or department not found / inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate email, license number, or employee ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — insufficient role")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> createDoctor(
            @Valid @RequestBody CreateDoctorRequestDto dto) {
        log.info("Received request to onboard doctor — email: '{}'", dto.email());
        DoctorResponseDto created = doctorService.createDoctor(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Doctor onboarded successfully"));
    }

    // -------------------------------------------------------------------------
    // GET /api/doctors — Paginated list
    // -------------------------------------------------------------------------

    @Operation(
            summary = "List all doctors (paginated)",
            description = "Returns a paginated list of all doctor summaries. Supports page/size/sort query parameters. " +
                    "Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, and DOCTOR."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor list returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<DoctorSummaryDto>>> getAllDoctors(

            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort field and direction, e.g. 'createdAt,desc'", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.debug("Received request to list doctors — page={}, size={}", page, size);
        PagedResponse<DoctorSummaryDto> result = doctorService.getAllDoctors(PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(result, "Doctors fetched successfully"));
    }

    // -------------------------------------------------------------------------
    // GET /api/doctors/by-employee-id/{employeeId} — Lookup by hospital employee ID
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Get doctor by employee ID",
            description = "Looks up a doctor by hospital-scoped employee ID (e.g. EMP-DOC-001). Requires hospitalId query param."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/by-employee-id/{employeeId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getDoctorByEmployeeId(
            @Parameter(description = "Hospital-scoped employee ID, e.g. EMP-DOC-001", required = true)
            @PathVariable String employeeId,
            @Parameter(description = "Hospital UUID for scoping", required = true)
            @RequestParam UUID hospitalId) {
        log.debug("Fetching doctor by employeeId: {} in hospital: {}", employeeId, hospitalId);
        return ResponseEntity.ok(ApiResponse.success(
                doctorService.getDoctorByEmployeeId(employeeId, hospitalId), "Doctor fetched successfully"));
    }

    // -------------------------------------------------------------------------
    // GET /api/doctors/{id} — Doctor detail
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Get doctor by ID",
            description = "Retrieves full doctor profile by UUID. Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, and DOCTOR."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> getDoctorById(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id) {
        log.debug("Received request to fetch doctor — ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(id), "Doctor fetched successfully"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/doctors/{id} — Update profile
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update doctor profile",
            description = "Partially updates a doctor's profile. Null fields are ignored. " +
                    "Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> updateDoctor(
            @Parameter(description = "UUID of the doctor to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDoctorRequestDto dto) {
        log.info("Received request to update doctor — ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(doctorService.updateDoctor(id, dto), "Doctor updated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/doctors/{id}/activate
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Activate a doctor",
            description = "Sets the doctor's isActive flag to true. Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor activated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateDoctor(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id) {
        log.info("Received request to activate doctor — ID: {}", id);
        doctorService.activateDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor activated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/doctors/{id}/deactivate
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Deactivate a doctor",
            description = "Soft-deactivates a doctor (sets isActive = false). Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Doctor deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateDoctor(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id) {
        log.info("Received request to deactivate doctor — ID: {}", id);
        doctorService.deactivateDoctor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor deactivated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/doctors/{id}/department — Reassign department
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Assign doctor to a department",
            description = "Moves a doctor to the specified department. Department must belong to the doctor's hospital. " +
                    "Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor or department not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Department belongs to a different hospital"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/department")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> assignDepartment(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody AssignDepartmentRequestDto dto) {
        log.info("Received request to assign department {} to doctor {}", dto.departmentId(), id);
        return ResponseEntity.ok(ApiResponse.success(
                doctorService.assignDepartment(id, dto.departmentId()), "Department assigned successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/doctors/{id}/availability — Toggle availability
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update doctor availability",
            description = "Sets whether a doctor is available for scheduling. " +
                    "Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, and DOCTOR (own profile)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Availability updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<Void>> updateAvailability(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id,
            @RequestBody UpdateAvailabilityRequestDto dto) {
        log.info("Received request to set availability={} for doctor {}", dto.available(), id);
        doctorService.updateAvailability(id, dto.available());
        return ResponseEntity.ok(ApiResponse.success(null, "Availability updated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/doctors/{id}/consultation-fee — Update fee
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update consultation fee",
            description = "Sets the doctor's consultation fee. Must be >= 0. Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fee updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error — fee is negative or malformed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/consultation-fee")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorResponseDto>> updateConsultationFee(
            @Parameter(description = "UUID of the doctor", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConsultationFeeRequestDto dto) {
        log.info("Received request to update consultation fee for doctor {}", id);
        return ResponseEntity.ok(ApiResponse.success(
                doctorService.updateConsultationFee(id, dto.fee()), "Consultation fee updated successfully"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }
}
