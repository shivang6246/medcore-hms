package com.medcore.hms.config;

import com.medcore.hms.common.entity.Address;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.entity.BloodGroup;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bootstraps the database with essential seed data on every application startup.
 * All operations are idempotent — safe to run multiple times.
 *
 * Seeds:
 *   1. 9 system roles
 *   2. 1 sample hospital
 *   3. 10 core departments
 *   4. 2 sample doctors
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("========== MedCore DataSeeder Starting ==========");
        seedRoles();
        Hospital hospital = seedSampleHospital();
        seedDepartments(hospital);
        seedSuperAdminUser(hospital);
        seedSampleDoctors(hospital);
        seedSamplePatients(hospital);
        log.info("========== MedCore DataSeeder Complete ==========");
    }

    // -------------------------------------------------------------------------
    // 1. Seed 9 System Roles
    // -------------------------------------------------------------------------

    private void seedRoles() {
        Map<RoleName, String> roleDescriptions = Map.of(
                RoleName.SUPER_ADMIN,      "Full system access — manages all hospitals",
                RoleName.HOSPITAL_ADMIN,   "Manages all data within their hospital",
                RoleName.DEPARTMENT_HEAD,  "Manages doctors within a department",
                RoleName.DOCTOR,           "Views and manages assigned patients",
                RoleName.NURSE,            "Assists doctors; limited patient access",
                RoleName.RECEPTIONIST,     "Patient registration and appointment scheduling",
                RoleName.PHARMACIST,       "Manages prescriptions and medicines",
                RoleName.LAB_TECHNICIAN,   "Manages lab reports and test results",
                RoleName.PATIENT,          "Can view only their own records"
        );

        int created = 0;
        for (Map.Entry<RoleName, String> entry : roleDescriptions.entrySet()) {
            if (!roleRepository.existsByName(entry.getKey())) {
                roleRepository.save(Role.builder()
                        .name(entry.getKey())
                        .description(entry.getValue())
                        .build());
                created++;
            }
        }
        log.info("✅ Roles: {} seeded, {} already existed", created, roleRepository.count() - created);
    }

    // -------------------------------------------------------------------------
    // 2. Seed Sample Hospital
    // -------------------------------------------------------------------------

    private Hospital seedSampleHospital() {
        final String regNumber = "HOSP-SAMPLE-001";
        return hospitalRepository.findByRegistrationNumber(regNumber).orElseGet(() -> {
            Hospital hospital = Hospital.builder()
                    .name("MedCore General Hospital")
                    .registrationNumber(regNumber)
                    .licenseNumber("LIC-SAMPLE-001")
                    .phone("+91-9876543210")
                    .email("admin@medcore-hospital.com")
                    .website("https://medcore-hospital.com")
                    .isActive(true)
                    .build();
            Hospital saved = hospitalRepository.save(hospital);
            log.info("✅ Sample hospital seeded: {}", saved.getName());
            return saved;
        });
    }

    // -------------------------------------------------------------------------
    // 3. Seed Core Departments
    // -------------------------------------------------------------------------

    private void seedDepartments(Hospital hospital) {
        List<String[]> departments = List.of(
                new String[]{"Cardiology",       "Heart and cardiovascular system specialists"},
                new String[]{"Neurology",        "Brain and nervous system specialists"},
                new String[]{"Emergency",        "24/7 emergency and trauma care"},
                new String[]{"Orthopedics",      "Bone, joint, and muscle specialists"},
                new String[]{"Pediatrics",       "Medical care for children and infants"},
                new String[]{"Gynecology",       "Women's reproductive health"},
                new String[]{"Radiology",        "Medical imaging and diagnostics"},
                new String[]{"Oncology",         "Cancer diagnosis and treatment"},
                new String[]{"Dermatology",      "Skin, hair, and nail conditions"},
                new String[]{"General Medicine", "General health and preventive care"}
        );

        int created = 0;
        for (String[] dept : departments) {
            if (!departmentRepository.existsByHospital_IdAndName(hospital.getId(), dept[0])) {
                departmentRepository.save(Department.builder()
                        .hospital(hospital)
                        .name(dept[0])
                        .description(dept[1])
                        .isActive(true)
                        .build());
                created++;
            }
        }
        log.info("✅ Departments: {} seeded for hospital '{}'", created, hospital.getName());
    }

    // -------------------------------------------------------------------------
    // 4. Seed Super Admin User
    // -------------------------------------------------------------------------

    private void seedSuperAdminUser(Hospital hospital) {
        final String superAdminEmail = "shivangv493@gmail.com";
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found"));

        userRepository.findByEmail(superAdminEmail).ifPresentOrElse(
                user -> {
                    user.setRoles(new HashSet<>(Set.of(superAdminRole)));
                    user.setIsActive(true);
                    user.setIsEmailVerified(true);
                    user.setHospital(hospital);
                    userRepository.save(user);
                    log.info("✅ Granted SUPER_ADMIN role to existing user: {}", superAdminEmail);
                },
                () -> {
                    User superAdmin = User.builder()
                            .hospital(hospital)
                            .firstName("Shivang")
                            .lastName("Admin")
                            .email(superAdminEmail)
                            .passwordHash(passwordEncoder.encode("Password123!"))
                            .phone("+1-555-0100")
                            .isActive(true)
                            .isEmailVerified(true)
                            .roles(new HashSet<>(Set.of(superAdminRole)))
                            .build();
                    userRepository.save(superAdmin);
                    log.info("✅ Super Admin user seeded: {}", superAdminEmail);
                }
        );
    }

    // -------------------------------------------------------------------------
    // 5. Seed Sample Doctors
    // -------------------------------------------------------------------------

    private void seedSampleDoctors(Hospital hospital) {
        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseThrow(() -> new IllegalStateException("DOCTOR role not found"));

        Department cardiology = departmentRepository
                .findByHospital_IdAndIsActiveTrue(hospital.getId())
                .stream()
                .filter(d -> d.getName().equals("Cardiology"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cardiology department not found"));

        Department generalMedicine = departmentRepository
                .findByHospital_IdAndIsActiveTrue(hospital.getId())
                .stream()
                .filter(d -> d.getName().equals("General Medicine"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("General Medicine department not found"));

        seedDoctor(
                hospital, cardiology, doctorRole,
                "dr.arjun.sharma@medcore-hospital.com",
                "Arjun", "Sharma",
                "EMP-DOC-001", "LIC-DOC-001",
                "Interventional Cardiology", "MD, DM (Cardiology)",
                Gender.MALE, LocalDate.of(1978, 3, 15),
                12, new BigDecimal("800.00")
        );

        seedDoctor(
                hospital, generalMedicine, doctorRole,
                "dr.priya.nair@medcore-hospital.com",
                "Priya", "Nair",
                "EMP-DOC-002", "LIC-DOC-002",
                "General Medicine", "MBBS, MD (General Medicine)",
                Gender.FEMALE, LocalDate.of(1985, 7, 22),
                8, new BigDecimal("500.00")
        );

        log.info("✅ Sample doctors seeded for hospital '{}'", hospital.getName());
    }

    private void seedDoctor(
            Hospital hospital, Department department, Role doctorRole,
            String email, String firstName, String lastName,
            String employeeId, String licenseNumber,
            String specialization, String qualification,
            Gender gender, LocalDate dob,
            int yearsOfExperience, BigDecimal consultationFee) {

        if (doctorRepository.existsByLicenseNumber(licenseNumber)) {
            return;
        }

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .hospital(hospital)
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .passwordHash(passwordEncoder.encode("Doctor@123!"))
                        .isActive(true)
                        .isEmailVerified(true)
                        .roles(new HashSet<>(Set.of(doctorRole)))
                        .build())
        );

        doctorRepository.save(Doctor.builder()
                .user(user)
                .hospital(hospital)
                .department(department)
                .employeeId(employeeId)
                .email(email)
                .gender(gender)
                .dateOfBirth(dob)
                .licenseNumber(licenseNumber)
                .specialization(specialization)
                .qualification(qualification)
                .yearsOfExperience(yearsOfExperience)
                .consultationFee(consultationFee)
                .isActive(true)
                .build());
    }

    // -------------------------------------------------------------------------
    // 6. Seed Sample Patients
    // -------------------------------------------------------------------------

    private void seedSamplePatients(Hospital hospital) {
        seedPatient(
                hospital,
                "P-2026-00001",
                "Aanya", "Mehta",
                LocalDate.of(1992, 5, 14),
                Gender.FEMALE,
                BloodGroup.B_POSITIVE,
                "+91-9811223344",
                "aanya.mehta@example.com",
                "Rajiv Mehta", "+91-9822334455", "Spouse",
                "Star Health Insurance", "STAR-2026-78901",
                "Penicillin",
                "Hypertension diagnosed 2020; managed with Amlodipine 5mg.",
                "12 MG Road", "Mumbai", "Maharashtra", "400001", "India"
        );

        seedPatient(
                hospital,
                "P-2026-00002",
                "Kiran", "Desai",
                LocalDate.of(1978, 11, 30),
                Gender.MALE,
                BloodGroup.O_POSITIVE,
                "+91-9833445566",
                null,
                "Sunita Desai", "+91-9844556677", "Wife",
                "HDFC Ergo Health", "HDFC-2026-11234",
                "Sulfa drugs, Aspirin",
                "Type 2 Diabetes since 2018; on Metformin 500mg twice daily.",
                "45 Park Street", "Bangalore", "Karnataka", "560001", "India"
        );

        log.info("✅ Sample patients seeded for hospital '{}'", hospital.getName());
    }

    private void seedPatient(
            Hospital hospital,
            String patientId,
            String firstName, String lastName,
            LocalDate dateOfBirth,
            Gender gender,
            BloodGroup bloodGroup,
            String phone,
            String email,
            String ecName, String ecPhone, String ecRelationship,
            String insuranceProvider, String insurancePolicyNumber,
            String allergies, String medicalHistory,
            String street, String city, String state, String postalCode, String country) {

        if (patientRepository.existsByPatientIdAndHospital_Id(patientId, hospital.getId())) {
            return;
        }

        Address address = Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country)
                .build();

        patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId(patientId)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .gender(gender)
                .bloodGroup(bloodGroup)
                .phone(phone)
                .email(email)
                .address(address)
                .emergencyContactName(ecName)
                .emergencyContactPhone(ecPhone)
                .emergencyContactRelationship(ecRelationship)
                .insuranceProvider(insuranceProvider)
                .insurancePolicyNumber(insurancePolicyNumber)
                .allergies(allergies)
                .medicalHistory(medicalHistory)
                .isActive(true)
                .build());
    }
}
