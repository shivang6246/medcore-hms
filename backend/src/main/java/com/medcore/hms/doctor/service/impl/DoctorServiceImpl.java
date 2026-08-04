package com.medcore.hms.doctor.service.impl;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.exception.DepartmentNotFoundException;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.dto.CreateDoctorRequestDto;
import com.medcore.hms.doctor.dto.DoctorResponseDto;
import com.medcore.hms.doctor.dto.DoctorSummaryDto;
import com.medcore.hms.doctor.dto.UpdateDoctorRequestDto;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.*;
import com.medcore.hms.doctor.mapper.DoctorMapper;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.service.DoctorService;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional
    public DoctorResponseDto createDoctor(CreateDoctorRequestDto dto) {
        log.info("Creating doctor — email: '{}'", dto.email());

        Hospital hospital = validateHospitalActive(dto.hospitalId());
        Department department = validateDepartmentActive(dto.departmentId());

        if (!department.getHospital().getId().equals(hospital.getId())) {
            log.warn("Department {} does not belong to hospital {}", dto.departmentId(), dto.hospitalId());
            throw new InvalidDepartmentAssignmentException(dto.departmentId(), dto.hospitalId());
        }

        validateNoDuplicateEmail(dto.email());
        validateNoDuplicateLicense(dto.licenseNumber());
        validateNoDuplicateEmployeeId(dto.employeeId(), dto.hospitalId());

        var doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseThrow(() -> new IllegalStateException("DOCTOR role not found"));

        User user = User.builder()
                .hospital(hospital)
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(dto.email())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .phone(dto.phone())
                .isActive(true)
                .isEmailVerified(false)
                .roles(new HashSet<>(Set.of(doctorRole)))
                .build();
        User savedUser = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(savedUser)
                .hospital(hospital)
                .department(department)
                .employeeId(dto.employeeId())
                .email(dto.email())
                .gender(dto.gender())
                .dateOfBirth(dto.dateOfBirth())
                .licenseNumber(dto.licenseNumber())
                .specialization(dto.specialization())
                .qualification(dto.qualification())
                .yearsOfExperience(dto.yearsOfExperience() != null ? dto.yearsOfExperience() : 0)
                .consultationFee(dto.consultationFee() != null ? dto.consultationFee() : BigDecimal.ZERO)
                .profileImageUrl(dto.profileImageUrl())
                .biography(dto.biography())
                .isActive(true)
                .isAvailable(true)
                .build();

        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor created — ID: {}, license: '{}'", saved.getId(), saved.getLicenseNumber());
        return doctorMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public DoctorResponseDto updateDoctor(UUID id, UpdateDoctorRequestDto dto) {
        log.info("Updating doctor — ID: {}", id);
        Doctor doctor = findOrThrow(id);
        doctorMapper.applyUpdate(dto, doctor);
        Doctor updated = doctorRepository.save(doctor);
        log.info("Doctor updated — ID: {}", id);
        return doctorMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDto getDoctorById(UUID id) {
        log.debug("Fetching doctor — ID: {}", id);
        return doctorMapper.toResponseDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DoctorSummaryDto> getAllDoctors(Pageable pageable) {
        int pageNumber = Math.max(0, pageable.getPageNumber());
        int pageSize   = Math.max(1, Math.min(100, pageable.getPageSize()));
        Pageable sanitized = PageRequest.of(pageNumber, pageSize, pageable.getSort());

        log.debug("Fetching all doctors — page={}, size={}", pageNumber, pageSize);
        Page<DoctorSummaryDto> page = doctorRepository.findAll(sanitized).map(doctorMapper::toSummaryDto);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponseDto getDoctorByUserId(UUID userId) {
        log.debug("Fetching doctor by user — userID: {}", userId);
        Doctor doctor = doctorRepository.findByUser_Id(userId)
                .orElseThrow(() -> {
                    log.warn("No doctor profile found for user — userID: {}", userId);
                    return new DoctorNotFoundException("No doctor profile found for user id: " + userId);
                });
        return doctorMapper.toResponseDto(doctor);
    }

    @Override
    @Transactional
    public void activateDoctor(UUID id) {
        log.info("Activating doctor — ID: {}", id);
        Doctor doctor = findOrThrow(id);
        doctor.setIsActive(true);
        doctorRepository.save(doctor);
        log.info("Doctor activated — ID: {}", id);
    }

    @Override
    @Transactional
    public void deactivateDoctor(UUID id) {
        log.info("Deactivating doctor — ID: {}", id);
        Doctor doctor = findOrThrow(id);
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
        log.info("Doctor deactivated — ID: {}", id);
    }

    @Override
    @Transactional
    public DoctorResponseDto assignDepartment(UUID doctorId, UUID departmentId) {
        log.info("Assigning department {} to doctor {}", departmentId, doctorId);
        Doctor doctor = findOrThrow(doctorId);
        Department department = validateDepartmentActive(departmentId);

        if (!department.getHospital().getId().equals(doctor.getHospital().getId())) {
            log.warn("Department {} does not belong to doctor's hospital {}", departmentId, doctor.getHospital().getId());
            throw new InvalidDepartmentAssignmentException(departmentId, doctor.getHospital().getId());
        }

        doctor.setDepartment(department);
        Doctor updated = doctorRepository.save(doctor);
        log.info("Department assigned — doctorID: {}, departmentID: {}", doctorId, departmentId);
        return doctorMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public DoctorResponseDto updateConsultationFee(UUID doctorId, BigDecimal fee) {
        log.info("Updating consultation fee for doctor {} to {}", doctorId, fee);
        Doctor doctor = findOrThrow(doctorId);
        doctor.setConsultationFee(fee);
        Doctor updated = doctorRepository.save(doctor);
        log.info("Consultation fee updated — doctorID: {}", doctorId);
        return doctorMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void updateAvailability(UUID doctorId, boolean available) {
        log.info("Setting availability={} for doctor {}", available, doctorId);
        Doctor doctor = findOrThrow(doctorId);
        doctor.setIsAvailable(available);
        doctorRepository.save(doctor);
        log.info("Availability updated — doctorID: {}, available: {}", doctorId, available);
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    private Hospital validateHospitalActive(UUID hospitalId) {
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

    private Department validateDepartmentActive(UUID departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> {
                    log.warn("Department not found — ID: {}", departmentId);
                    return new DepartmentNotFoundException(departmentId);
                });
        if (!department.getIsActive()) {
            log.warn("Department is inactive — ID: {}", departmentId);
            throw new DepartmentNotFoundException(departmentId);
        }
        return department;
    }

    private void validateNoDuplicateEmail(String email) {
        if (doctorRepository.existsByEmail(email)) {
            log.warn("Duplicate doctor email: '{}'", email);
            throw new DuplicateDoctorEmailException(email);
        }
    }

    private void validateNoDuplicateLicense(String licenseNumber) {
        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            log.warn("Duplicate license number: '{}'", licenseNumber);
            throw new DuplicateLicenseNumberException(licenseNumber);
        }
    }

    private void validateNoDuplicateEmployeeId(String employeeId, UUID hospitalId) {
        if (doctorRepository.existsByEmployeeIdAndHospital_Id(employeeId, hospitalId)) {
            log.warn("Duplicate employee ID '{}' in hospital {}", employeeId, hospitalId);
            throw new DuplicateEmployeeIdException(employeeId, hospitalId);
        }
    }

    private Doctor findOrThrow(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Doctor not found — ID: {}", id);
                    return new DoctorNotFoundException(id);
                });
    }
}
