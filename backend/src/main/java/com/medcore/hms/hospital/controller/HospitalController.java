package com.medcore.hms.hospital.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import com.medcore.hms.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Hospital lifecycle management.
 *
 * <p>Exposes CRUD endpoints for hospital tenants.
 * All endpoints are protected by JWT authentication.
 * Fine-grained RBAC is enforced via {@code @PreAuthorize}.</p>
 *
 * <ul>
 *   <li>SUPER_ADMIN — full access (create, read, update, activate, deactivate)</li>
 *   <li>HOSPITAL_ADMIN — read list + update own hospital</li>
 *   <li>DOCTOR / NURSE / DEPARTMENT_HEAD — read own hospital only</li>
 * </ul>
 */
@Tag(name = "Hospital Management", description = "REST APIs for hospital onboarding, updates, paginated search/filter, and lifecycle management.")
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    // -------------------------------------------------------------------------
    // GET /api/hospitals — Paginated, searchable, filterable list
    // -------------------------------------------------------------------------

    @Operation(
            summary = "List hospitals (paginated + search + filter + sort)",
            description = "Retrieves a paginated list of hospitals. Supports keyword search across " +
                    "name, regNumber, licenseNumber, email, and city; filtering by isActive/city; and custom sorting."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated hospitals returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — insufficient role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<HospitalSummaryDto>>> getHospitals(

            @Parameter(description = "Keyword search across name, regNumber, licenseNumber, email, and city")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by active (true) or inactive (false) status")
            @RequestParam(required = false) Boolean isActive,

            @Parameter(description = "Filter by hospital address city")
            @RequestParam(required = false) String city,

            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort property and direction, e.g. 'name,asc' or 'createdAt,desc'", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        PagedResponse<HospitalSummaryDto> pagedData = hospitalService.getHospitals(search, isActive, city, pageable);
        return ResponseEntity.ok(ApiResponse.success(pagedData, "Hospitals fetched successfully"));
    }

    // -------------------------------------------------------------------------
    // GET /api/hospitals/{id} — Full hospital detail
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Get hospital by ID",
            description = "Retrieves full hospital details by UUID. Accessible by SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR, NURSE, and DEPARTMENT_HEAD."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'DEPARTMENT_HEAD')")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> getById(
            @Parameter(description = "UUID of the hospital", required = true)
            @PathVariable UUID id) {
        HospitalResponseDto hospital = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(ApiResponse.success(hospital, "Hospital details fetched successfully"));
    }

    // -------------------------------------------------------------------------
    // POST /api/hospitals — Onboard a new hospital
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Onboard a new hospital",
            description = "Creates a new tenant hospital. Only SUPER_ADMIN can onboard hospitals."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Hospital onboarded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict — duplicate registration number, license number, or email"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden — only SUPER_ADMIN allowed")
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> create(
            @Valid @RequestBody CreateHospitalRequestDto dto) {
        HospitalResponseDto created = hospitalService.createHospital(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Hospital onboarded successfully"));
    }

    // -------------------------------------------------------------------------
    // PUT /api/hospitals/{id} — Update hospital details
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Update hospital details",
            description = "Updates mutable fields of an existing hospital. Null fields are ignored (partial update). " +
                    "Accessible by SUPER_ADMIN and HOSPITAL_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict — duplicate field value"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> update(
            @Parameter(description = "UUID of the hospital to update", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHospitalRequestDto dto) {
        HospitalResponseDto updated = hospitalService.updateHospital(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Hospital details updated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/hospitals/{id}/deactivate — Soft deactivate
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Deactivate a hospital",
            description = "Soft-deactivates a hospital tenant (sets isActive = false). Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateHospital(
            @Parameter(description = "UUID of the hospital to deactivate", required = true)
            @PathVariable UUID id) {
        hospitalService.deactivateHospital(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hospital deactivated successfully"));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/hospitals/{id}/activate — Reactivate
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Activate a hospital",
            description = "Re-activates a previously deactivated hospital tenant (sets isActive = true). Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateHospital(
            @Parameter(description = "UUID of the hospital to activate", required = true)
            @PathVariable UUID id) {
        hospitalService.activateHospital(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hospital activated successfully"));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/hospitals/{id} — RESTful soft-delete alias
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Deactivate a hospital (DELETE alias)",
            description = "Soft-deactivates a hospital tenant using the standard HTTP DELETE verb. " +
                    "Functionally identical to PATCH /{id}/deactivate. Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hospital deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Hospital not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(
            @Parameter(description = "UUID of the hospital to deactivate", required = true)
            @PathVariable UUID id) {
        hospitalService.deactivateHospital(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Hospital deactivated successfully"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Parses a sort string like {@code "name,asc"} or {@code "createdAt,desc"}
     * into a Spring Data {@link Sort} object. Defaults to descending {@code createdAt}.
     */
    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
