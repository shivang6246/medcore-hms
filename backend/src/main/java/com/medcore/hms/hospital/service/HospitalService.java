package com.medcore.hms.hospital.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service contract for Hospital lifecycle management.
 *
 * <p>
 * Provides operations for creating, updating, querying,
 * activating and deactivating hospital tenants. All read operations
 * are scoped appropriately via RBAC at the controller layer.
 * </p>
 */
public interface HospitalService {

    /** Onboard a new hospital tenant. */
    HospitalResponseDto createHospital(CreateHospitalRequestDto dto);

    /**
     * Update mutable fields of an existing hospital. Ignores null fields (partial
     * update).
     */
    HospitalResponseDto updateHospital(UUID id, UpdateHospitalRequestDto dto);

    /** Retrieve full hospital details by ID. */
    HospitalResponseDto getHospitalById(UUID id);

    /** Retrieve a flat summary list of all hospitals (no pagination). */
    List<HospitalSummaryDto> getAllHospitals();

    /**
     * Paginated, searchable and filterable list of hospitals.
     *
     * @param search   keyword matched against name, regNumber, licenseNumber,
     *                 email, city
     * @param isActive filter by active status; null means no filter
     * @param city     partial city name filter; null means no filter
     * @param pageable pagination and sort configuration
     */
    PagedResponse<HospitalSummaryDto> getHospitals(String search, Boolean isActive, String city, Pageable pageable);

    /** Soft-deactivate a hospital (sets isActive = false). */
    void deactivateHospital(UUID id);

    /** Re-activate a previously deactivated hospital (sets isActive = true). */
    void activateHospital(UUID id);
}
