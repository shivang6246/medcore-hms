package com.medcore.hms.lab.repository;

import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.lab.entity.LabReport;
import com.medcore.hms.lab.entity.LabTest;
import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.entity.TestPriority;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("LabTestRepository & LabReportRepository — Integration Tests")
class LabTestRepositoryTest {

    @Autowired
    private LabTestRepository labTestRepository;
    @Autowired
    private LabReportRepository labReportRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;

    private Patient patient;
    private Doctor doctor;
    private LabTest labTest;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("St. Jude Hospital")
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("stjude_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        Department department = departmentRepository.save(Department.builder()
                .name("Pathology " + UUID.randomUUID().toString().substring(0, 5))
                .hospital(hospital)
                .isActive(true)
                .build());

        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.DOCTOR).description("Doctor").build()));

        User doctorUser = userRepository.save(User.builder()
                .firstName("Meredith")
                .lastName("Grey")
                .email("grey_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .passwordHash("password123")
                .roles(Set.of(doctorRole))
                .hospital(hospital)
                .isActive(true)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .user(doctorUser)
                .hospital(hospital)
                .department(department)
                .employeeId("EMP-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("DOCLIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email(doctorUser.getEmail())
                .specialization("General Surgery")
                .isActive(true)
                .build());

        patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Derek")
                .lastName("Shepherd")
                .dateOfBirth(LocalDate.of(1975, 3, 26))
                .phone("98765432" + (int)(Math.random()*100))
                .email("derek_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("Meredith Grey")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        labTest = labTestRepository.save(LabTest.builder()
                .patient(patient)
                .doctor(doctor)
                .testType("Liver Function Test (LFT)")
                .priority(TestPriority.NORMAL)
                .status(LabTestStatus.REQUESTED)
                .instructions("Overnight fasting")
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Should find lab tests by Patient ID")
    void findByPatient_Id_Success() {
        Page<LabTest> page = labTestRepository.findByPatient_Id(patient.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getTestType()).isEqualTo("Liver Function Test (LFT)");
    }

    @Test
    @DisplayName("Should find lab tests by status")
    void findByStatus_Success() {
        Page<LabTest> page = labTestRepository.findByStatus(LabTestStatus.REQUESTED, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should save lab report and link with lab test")
    void saveLabReport_Success() {
        LabReport report = labReportRepository.save(LabReport.builder()
                .labTest(labTest)
                .result("ALT: 25 U/L, AST: 20 U/L. Normal.")
                .remarks("Healthy liver profile")
                .reportedAt(LocalDateTime.now())
                .build());

        Optional<LabReport> found = labReportRepository.findByLabTest_Id(labTest.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getResult()).contains("ALT: 25 U/L");
    }
}
