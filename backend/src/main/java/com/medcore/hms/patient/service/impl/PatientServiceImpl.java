package com.medcore.hms.patient.service.impl;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.common.entity.Address;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.dto.*;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.DuplicatePatientEmailException;
import com.medcore.hms.patient.exception.DuplicatePatientPhoneException;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.mapper.PatientMapper;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional
    public PatientResponseDto createPatient(CreatePatientRequestDto dto) {
        log.info("Creating patient — hospital: {}, name: {} {}", dto.hospitalId(), dto.firstName(), dto.lastName());

        Hospital hospital = findActiveHospitalOrThrow(dto.hospitalId());

        if (dto.phone() != null) {
            validatePhoneUnique(dto.phone(), dto.hospitalId());
        }
        if (dto.email() != null && !dto.email().isBlank()) {
            validateEmailUnique(dto.email());
        }

        String patientId = generatePatientId(dto.hospitalId());

        Address address = null;
        if (dto.address() != null) {
            address = Address.builder()
                    .street(dto.address().street())
                    .city(dto.address().city())
                    .state(dto.address().state())
                    .postalCode(dto.address().postalCode())
                    .country(dto.address().country())
                    .build();
        }

        Patient patient = Patient.builder()
                .hospital(hospital)
                .patientId(patientId)
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .dateOfBirth(dto.dateOfBirth())
                .gender(dto.gender())
                .bloodGroup(dto.bloodGroup())
                .phone(dto.phone())
                .email(dto.email())
                .address(address)
                .emergencyContactName(dto.emergencyContactName())
                .emergencyContactPhone(dto.emergencyContactPhone())
                .emergencyContactRelationship(dto.emergencyContactRelationship())
                .insuranceProvider(dto.insuranceProvider())
                .insurancePolicyNumber(dto.insurancePolicyNumber())
                .allergies(dto.allergies())
                .medicalHistory(dto.medicalHistory())
                .isActive(true)
                .build();

        Patient saved = patientRepository.save(patient);
        log.info("Patient created — ID: {}, patientId: {}", saved.getId(), saved.getPatientId());
        return patientMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public PatientResponseDto updatePatient(UUID id, UpdatePatientRequestDto dto) {
        log.info("Updating patient — ID: {}", id);
        Patient patient = findOrThrow(id);

        if (dto.phone() != null && !dto.phone().equals(patient.getPhone())) {
            if (patientRepository.existsByPhoneAndHospital_IdAndIdNot(dto.phone(), patient.getHospital().getId(), id)) {
                log.warn("Duplicate phone '{}' in hospital {}", dto.phone(), patient.getHospital().getId());
                throw new DuplicatePatientPhoneException(dto.phone());
            }
        }
        if (dto.email() != null && !dto.email().equals(patient.getEmail())) {
            if (patientRepository.existsByEmailAndIdNot(dto.email(), id)) {
                log.warn("Duplicate email '{}'", dto.email());
                throw new DuplicatePatientEmailException(dto.email());
            }
        }

        patientMapper.applyUpdate(dto, patient);
        Patient updated = patientRepository.save(patient);
        log.info("Patient updated — ID: {}", id);
        return patientMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getPatientById(UUID id) {
        log.debug("Fetching patient — ID: {}", id);
        return patientMapper.toResponseDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getPatientByPatientId(String patientId, UUID hospitalId) {
        log.debug("Fetching patient by patientId: {} in hospital: {}", patientId, hospitalId);
        Patient patient = patientRepository.findByPatientIdAndHospital_Id(patientId, hospitalId)
                .orElseThrow(() -> {
                    log.warn("Patient not found — patientId: {}", patientId);
                    return new PatientNotFoundException("Patient not found with patientId: " + patientId);
                });
        return patientMapper.toResponseDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getPatientByEmail(String email) {
        log.debug("Fetching patient by email: {}", email);
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Patient not found for email: {}", email);
                    return new PatientNotFoundException("No patient profile found for email: " + email);
                });
        return patientMapper.toResponseDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientSummaryDto> getAllPatients(UUID hospitalId, Pageable pageable) {
        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.max(1, Math.min(100, pageable.getPageSize()));
        Pageable sanitized = PageRequest.of(page, size, pageable.getSort());

        log.debug("Fetching all patients — hospital: {}, page: {}, size: {}", hospitalId, page, size);
        Page<PatientSummaryDto> result = patientRepository
                .findByHospital_Id(hospitalId, sanitized)
                .map(patientMapper::toSummaryDto);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientSummaryDto> searchPatients(PatientSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching patients — criteria: {}", criteria);

        int page = Math.max(0, pageable.getPageNumber());
        int size = Math.max(1, Math.min(100, pageable.getPageSize()));
        Pageable sanitized = PageRequest.of(page, size, pageable.getSort());

        List<Patient> all = criteria.hospitalId() != null
                ? patientRepository.findByHospital_IdAndIsActiveTrue(criteria.hospitalId())
                : patientRepository.findAll();

        List<Patient> filtered = all.stream()
                .filter(p -> criteria.isActive()   == null || p.getIsActive().equals(criteria.isActive()))
                .filter(p -> criteria.bloodGroup() == null || criteria.bloodGroup().equals(p.getBloodGroup()))
                .filter(p -> criteria.patientId()  == null || p.getPatientId().equalsIgnoreCase(criteria.patientId()))
                .filter(p -> criteria.phone()      == null || p.getPhone().contains(criteria.phone()))
                .filter(p -> criteria.email()      == null || (p.getEmail() != null && p.getEmail().equalsIgnoreCase(criteria.email())))
                .filter(p -> criteria.name()       == null || matchesName(p, criteria.name()))
                .toList();

        int start = (int) sanitized.getOffset();
        int end   = Math.min(start + sanitized.getPageSize(), filtered.size());
        List<PatientSummaryDto> pageContent = (start > filtered.size())
                ? List.of()
                : filtered.subList(start, end).stream().map(patientMapper::toSummaryDto).toList();

        Page<PatientSummaryDto> springPage = new PageImpl<>(pageContent, sanitized, filtered.size());
        return PagedResponse.from(springPage);
    }

    @Override
    @Transactional
    public void activatePatient(UUID id) {
        log.info("Activating patient — ID: {}", id);
        Patient patient = findOrThrow(id);
        patient.setIsActive(true);
        patientRepository.save(patient);
        log.info("Patient activated — ID: {}", id);
    }

    @Override
    @Transactional
    public void deactivatePatient(UUID id) {
        log.info("Deactivating patient — ID: {}", id);
        Patient patient = findOrThrow(id);
        patient.setIsActive(false);
        patientRepository.save(patient);
        log.info("Patient deactivated — ID: {}", id);
    }

    private Patient findOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Patient not found — ID: {}", id);
                    return new PatientNotFoundException(id);
                });
    }

    private Hospital findActiveHospitalOrThrow(UUID hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> {
                    log.warn("Hospital not found — ID: {}", hospitalId);
                    return new HospitalNotFoundException(hospitalId);
                });
        if (!hospital.getIsActive()) {
            log.warn("Hospital is inactive — ID: {}", hospitalId);
            throw new HospitalNotFoundException(hospitalId);
        }
        return hospital;
    }

    private void validatePhoneUnique(String phone, UUID hospitalId) {
        if (patientRepository.existsByPhoneAndHospital_Id(phone, hospitalId)) {
            log.warn("Duplicate patient phone '{}' in hospital {}", phone, hospitalId);
            throw new DuplicatePatientPhoneException(phone);
        }
    }

    private void validateEmailUnique(String email) {
        if (patientRepository.existsByEmail(email)) {
            log.warn("Duplicate patient email '{}'", email);
            throw new DuplicatePatientEmailException(email);
        }
    }

    private String generatePatientId(UUID hospitalId) {
        int year = Year.now().getValue();
        long count = patientRepository.findByHospital_IdAndIsActiveTrue(hospitalId).size() + 1;
        return String.format("P-%d-%05d", year, count);
    }

    private boolean matchesName(Patient p, String name) {
        String full = (p.getFirstName() + " " + p.getLastName()).toLowerCase();
        return full.contains(name.toLowerCase())
                || p.getFirstName().toLowerCase().contains(name.toLowerCase())
                || p.getLastName().toLowerCase().contains(name.toLowerCase());
    }
}
