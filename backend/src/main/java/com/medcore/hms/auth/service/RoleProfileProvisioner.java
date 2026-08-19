package com.medcore.hms.auth.service;

import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Creates role-specific profile rows ({@code patient}, {@code doctor}) after a user
 * is persisted in {@code app_user}. Login identity always lives in app_user;
 * clinical/staff profiles live in their own tables and are linked by email / user_id.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleProfileProvisioner {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void ensureProfiles(User user) {
        if (user == null || user.getRoles() == null) {
            return;
        }

        boolean isPatient = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.PATIENT);
        boolean isDoctor = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR);

        if (isPatient) {
            ensurePatientProfile(user);
        }
        if (isDoctor) {
            ensureDoctorProfile(user);
        }
    }

    private void ensurePatientProfile(User user) {
        if (patientRepository.existsByEmail(user.getEmail())) {
            return;
        }

        Hospital hospital = resolveHospital(user);
        attachHospitalIfMissing(user, hospital);

        String requestedPhone = normalizePhone(user.getPhone(), user.getId());
        String phone = requestedPhone;
        if (patientRepository.existsByPhoneAndHospital_Id(requestedPhone, hospital.getId())) {
            phone = uniquePlaceholderPhone(user.getId());
            int guard = 0;
            while (patientRepository.existsByPhoneAndHospital_Id(phone, hospital.getId()) && guard++ < 8) {
                phone = uniquePlaceholderPhone(UUID.randomUUID());
            }
        }

        String patientId = generatePatientId(hospital.getId());

        Patient patient = Patient.builder()
                .hospital(hospital)
                .patientId(patientId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phone(phone)
                .email(user.getEmail())
                .emergencyContactName("To be updated")
                .emergencyContactPhone(phone)
                .emergencyContactRelationship("Self")
                .isActive(true)
                .build();

        patientRepository.saveAndFlush(patient);
        log.info("Provisioned patient profile {} for user {}", patientId, user.getEmail());
    }

    private void ensureDoctorProfile(User user) {
        if (doctorRepository.existsByEmail(user.getEmail())
                || doctorRepository.findByUser_Id(user.getId()).isPresent()) {
            return;
        }

        Hospital hospital = resolveHospital(user);
        attachHospitalIfMissing(user, hospital);
        Department department = resolveDepartment(hospital);

        String suffix = shortId(user.getId());
        String employeeId = "EMP-REG-" + suffix;
        String licenseNumber = "LIC-REG-" + suffix;

        while (doctorRepository.existsByEmployeeIdAndHospital_Id(employeeId, hospital.getId())) {
            suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            employeeId = "EMP-REG-" + suffix;
        }
        while (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            licenseNumber = "LIC-REG-" + suffix;
        }

        Doctor doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .department(department)
                .employeeId(employeeId)
                .email(user.getEmail())
                .licenseNumber(licenseNumber)
                .specialization("Pending assignment")
                .qualification("To be updated")
                .yearsOfExperience(0)
                .consultationFee(BigDecimal.ZERO)
                .isActive(true)
                .isAvailable(true)
                .build();

        doctorRepository.save(doctor);
        log.info("Provisioned doctor profile {} for user {}", employeeId, user.getEmail());
    }

    private Hospital resolveHospital(User user) {
        if (user.getHospital() != null) {
            return user.getHospital();
        }
        List<Hospital> hospitals = hospitalRepository.findAll();
        if (hospitals.isEmpty()) {
            throw new IllegalStateException("No hospital found. Run DataSeeder before registering users.");
        }
        return hospitals.stream()
                .filter(h -> "MedCore General Hospital".equalsIgnoreCase(h.getName()))
                .findFirst()
                .orElse(hospitals.get(0));
    }

    private Department resolveDepartment(Hospital hospital) {
        List<Department> departments = departmentRepository.findByHospital_IdAndIsActiveTrue(hospital.getId());
        if (departments.isEmpty()) {
            throw new IllegalStateException("No department found for hospital " + hospital.getName());
        }
        return departments.stream()
                .filter(d -> "General Medicine".equalsIgnoreCase(d.getName()))
                .findFirst()
                .orElse(departments.get(0));
    }

    private void attachHospitalIfMissing(User user, Hospital hospital) {
        if (user.getHospital() == null) {
            user.setHospital(hospital);
            userRepository.save(user);
        }
    }

    private String generatePatientId(UUID hospitalId) {
        int year = Year.now().getValue();
        long count = patientRepository.countByHospital_Id(hospitalId) + 1;
        String patientId = String.format("P-%d-%05d", year, count);

        // Retry with incrementing counter if ID already exists (handles race conditions / inactive gaps)
        int maxRetries = 20;
        while (patientRepository.existsByPatientIdAndHospital_Id(patientId, hospitalId) && maxRetries-- > 0) {
            count++;
            patientId = String.format("P-%d-%05d", year, count);
        }
        return patientId;
    }

    private String normalizePhone(String phone, UUID userId) {
        if (phone != null && phone.matches("^\\+?[0-9\\-\\s]{7,20}$")) {
            return phone.trim();
        }
        return uniquePlaceholderPhone(userId);
    }

    private String uniquePlaceholderPhone(UUID userId) {
        long n = Math.abs(userId.getMostSignificantBits() % 10_000_000_000L);
        return String.format("+91%010d", n);
    }

    private static String shortId(UUID id) {
        return id.toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
