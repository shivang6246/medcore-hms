package com.medcore.hms.config;

import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Bootstraps the database with essential seed data on every application startup.
 * All operations are idempotent — safe to run multiple times.
 *
 * Seeds:
 *   1. 9 system roles (SUPER_ADMIN → PATIENT)
 *   2. 1 sample hospital ("MedCore General Hospital")
 *   3. 10 core departments (Cardiology, Neurology, Emergency, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final com.medcore.hms.user.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("========== MedCore DataSeeder Starting ==========");
        seedRoles();
        Hospital hospital = seedSampleHospital();
        seedDepartments(hospital);
        seedSuperAdminUser(hospital);
        log.info("========== MedCore DataSeeder Complete ==========");
    }

    private void seedSuperAdminUser(Hospital hospital) {
        final String superAdminEmail = "shivangv493@gmail.com";
        Role superAdminRole = roleRepository.findByName(RoleName.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role not found"));

        userRepository.findByEmail(superAdminEmail).ifPresentOrElse(
                user -> {
                    user.getRoles().add(superAdminRole);
                    user.setIsActive(true);
                    user.setIsEmailVerified(true);
                    userRepository.save(user);
                    log.info("✅ Granted SUPER_ADMIN role to existing user: {}", superAdminEmail);
                },
                () -> {
                    com.medcore.hms.user.entity.User superAdmin = com.medcore.hms.user.entity.User.builder()
                            .hospital(hospital)
                            .firstName("Shivang")
                            .lastName("Admin")
                            .email(superAdminEmail)
                            .passwordHash(passwordEncoder.encode("Password123!"))
                            .phone("+1-555-0100")
                            .isActive(true)
                            .isEmailVerified(true)
                            .roles(java.util.Set.of(superAdminRole))
                            .build();

                    userRepository.save(superAdmin);
                    log.info("✅ Super Admin user seeded: {}", superAdminEmail);
                }
        );
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
                roleRepository.save(
                        Role.builder()
                                .name(entry.getKey())
                                .description(entry.getValue())
                                .build()
                );
                created++;
                log.debug("  [ROLE] Seeded: {}", entry.getKey());
            }
        }
        log.info("✅ Roles: {} seeded, {} already existed",
                created, roleRepository.count() - created);
    }

    // -------------------------------------------------------------------------
    // 2. Seed Sample Hospital
    // -------------------------------------------------------------------------

    private Hospital seedSampleHospital() {
        final String regNumber = "HOSP-SAMPLE-001";

        return hospitalRepository.findByRegistrationNumber(regNumber)
                .orElseGet(() -> {
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
                departmentRepository.save(
                        Department.builder()
                                .hospital(hospital)
                                .name(dept[0])
                                .description(dept[1])
                                .isActive(true)
                                .build()
                );
                created++;
                log.debug("  [DEPT] Seeded: {}", dept[0]);
            }
        }
        log.info("✅ Departments: {} seeded for hospital '{}'", created, hospital.getName());
    }
}
