package com.medcore.hms.hospital.service.impl;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.exception.DuplicateHospitalEmailException;
import com.medcore.hms.hospital.exception.DuplicateLicenseNumberException;
import com.medcore.hms.hospital.exception.DuplicateRegistrationNumberException;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.mapper.HospitalMapper;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.hospital.repository.HospitalSpecification;
import com.medcore.hms.hospital.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Primary implementation of {@link HospitalService}.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Registration number, license number, and email must be globally unique.</li>
 *   <li>Deactivation is a soft operation (sets {@code isActive = false}).</li>
 *   <li>Cache key {@code "hospitals"} is evicted on any write operation.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;

    // -------------------------------------------------------------------------
    // Write Operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public HospitalResponseDto createHospital(CreateHospitalRequestDto dto) {
        log.info("Initiating hospital creation — name: '{}', regNumber: '{}'", dto.name(), dto.registrationNumber());
        validateUniqueConstraints(dto.registrationNumber(), dto.licenseNumber(), dto.email());

        Hospital entity = hospitalMapper.toEntity(dto);
        Hospital saved  = hospitalRepository.save(entity);
        log.info("Hospital created — ID: {}, name: '{}'", saved.getId(), saved.getName());
        return hospitalMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public HospitalResponseDto updateHospital(UUID id, UpdateHospitalRequestDto dto) {
        log.info("Updating hospital — ID: {}", id);
        Hospital hospital = findOrThrow(id);
        validateUpdateConstraints(hospital, dto);

        hospitalMapper.applyUpdate(dto, hospital);
        Hospital updated = hospitalRepository.save(hospital);
        log.info("Hospital updated — ID: {}", updated.getId());
        return hospitalMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public void deactivateHospital(UUID id) {
        log.info("Deactivating hospital — ID: {}", id);
        Hospital hospital = findOrThrow(id);
        hospital.setIsActive(false);
        hospitalRepository.save(hospital);
        log.info("Hospital deactivated — ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "hospitals", allEntries = true)
    public void activateHospital(UUID id) {
        log.info("Activating hospital — ID: {}", id);
        Hospital hospital = findOrThrow(id);
        hospital.setIsActive(true);
        hospitalRepository.save(hospital);
        log.info("Hospital activated — ID: {}", id);
    }

    // -------------------------------------------------------------------------
    // Read Operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "hospitals", key = "#id")
    public HospitalResponseDto getHospitalById(UUID id) {
        log.debug("Fetching hospital details — ID: {}", id);
        return hospitalMapper.toResponseDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HospitalSummaryDto> getAllHospitals() {
        log.debug("Fetching all hospitals summary list");
        return hospitalRepository.findAll()
                .stream()
                .map(hospitalMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<HospitalSummaryDto> getHospitals(
            String search, Boolean isActive, String city, Pageable pageable) {

        // Sanitize & clamp page bounds to prevent abuse
        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize   = Math.max(1, Math.min(100, pageable.getPageSize()));
        Pageable sanitized = PageRequest.of(pageNumber, pageSize, pageable.getSort());

        log.info("Hospital search — search='{}', isActive={}, city='{}', page={}, size={}, sort='{}'",
                search, isActive, city, pageNumber, pageSize, sanitized.getSort());

        Specification<Hospital> spec = HospitalSpecification.filterHospitals(search, isActive, city);
        Page<HospitalSummaryDto> pageResult = hospitalRepository.findAll(spec, sanitized)
                .map(hospitalMapper::toSummaryDto);

        log.info("Hospital search result — {} elements, {} total pages ({} total)",
                pageResult.getNumberOfElements(), pageResult.getTotalPages(), pageResult.getTotalElements());

        return PagedResponse.from(pageResult);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Validates uniqueness constraints for hospital creation.
     * All three fields must be globally unique before we persist.
     */
    private void validateUniqueConstraints(String regNumber, String licenseNumber, String email) {
        if (hospitalRepository.existsByRegistrationNumber(regNumber)) {
            log.warn("Uniqueness check failed — duplicate regNumber: '{}'", regNumber);
            throw new DuplicateRegistrationNumberException(regNumber);
        }
        if (hospitalRepository.existsByLicenseNumber(licenseNumber)) {
            log.warn("Uniqueness check failed — duplicate licenseNumber: '{}'", licenseNumber);
            throw new DuplicateLicenseNumberException(licenseNumber);
        }
        if (email != null && hospitalRepository.existsByEmail(email)) {
            log.warn("Uniqueness check failed — duplicate email: '{}'", email);
            throw new DuplicateHospitalEmailException(email);
        }
    }

    /**
     * Validates uniqueness constraints for hospital update.
     * Only checks fields that are being changed (null = keep existing).
     */
    private void validateUpdateConstraints(Hospital hospital, UpdateHospitalRequestDto dto) {
        if (dto.registrationNumber() != null
                && !dto.registrationNumber().equals(hospital.getRegistrationNumber())
                && hospitalRepository.existsByRegistrationNumber(dto.registrationNumber())) {
            log.warn("Update uniqueness check failed — duplicate regNumber: '{}'", dto.registrationNumber());
            throw new DuplicateRegistrationNumberException(dto.registrationNumber());
        }
        if (dto.licenseNumber() != null
                && !dto.licenseNumber().equals(hospital.getLicenseNumber())
                && hospitalRepository.existsByLicenseNumber(dto.licenseNumber())) {
            log.warn("Update uniqueness check failed — duplicate licenseNumber: '{}'", dto.licenseNumber());
            throw new DuplicateLicenseNumberException(dto.licenseNumber());
        }
        if (dto.email() != null
                && !dto.email().equals(hospital.getEmail())
                && hospitalRepository.existsByEmail(dto.email())) {
            log.warn("Update uniqueness check failed — duplicate email: '{}'", dto.email());
            throw new DuplicateHospitalEmailException(dto.email());
        }
    }

    /**
     * Loads a Hospital entity by ID or throws {@link HospitalNotFoundException}.
     */
    private Hospital findOrThrow(UUID id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Hospital not found — ID: {}", id);
                    return new HospitalNotFoundException(id);
                });
    }
}
