package com.medcore.hms.medicalrecord.repository;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.doctor.slot.entity.SlotStatus;
import com.medcore.hms.doctor.slot.repository.DoctorSlotRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
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
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("MedicalRecordRepository — Integration Tests")
class MedicalRecordRepositoryTest {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
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
    @Autowired
    private DoctorSlotRepository doctorSlotRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    private Hospital hospital;
    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        hospital = hospitalRepository.save(Hospital.builder()
                .name("City General Hospital")
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("citygen_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        Department department = departmentRepository.save(Department.builder()
                .name("Cardiology " + UUID.randomUUID().toString().substring(0, 5))
                .hospital(hospital)
                .isActive(true)
                .build());

        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.DOCTOR).description("Doctor").build()));

        User doctorUser = userRepository.save(User.builder()
                .firstName("Gregory")
                .lastName("House")
                .email("house_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .passwordHash("password123")
                .roles(java.util.Set.of(doctorRole))
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
                .specialization("Diagnostics")
                .isActive(true)
                .build());

        patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("James")
                .lastName("Wilson")
                .dateOfBirth(LocalDate.of(1985, 5, 15))
                .phone("98765432" + (int)(Math.random()*100))
                .email("wilson_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("Mary Wilson")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        DoctorSlot slot = doctorSlotRepository.save(DoctorSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(SlotStatus.BOOKED)
                .build());

        appointment = appointmentRepository.save(Appointment.builder()
                .appointmentNumber("APT-" + UUID.randomUUID().toString().substring(0, 8))
                .hospital(hospital)
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(AppointmentStatus.COMPLETED)
                .build());

        medicalRecord = medicalRecordRepository.save(MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .symptoms("Persistent pain")
                .diagnosis("Lupus")
                .treatmentPlan("Steroids")
                .notes("Check back in a week")
                .followUpDate(LocalDate.now().plusDays(7))
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Should find medical record by appointment ID")
    void findByAppointment_Id_Success() {
        Optional<MedicalRecord> found = medicalRecordRepository.findByAppointment_Id(appointment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDiagnosis()).isEqualTo("Lupus");
        assertThat(found.get().getPatient().getId()).isEqualTo(patient.getId());
    }

    @Test
    @DisplayName("Should check existence by appointment ID")
    void existsByAppointment_Id_Success() {
        boolean exists = medicalRecordRepository.existsByAppointment_Id(appointment.getId());
        assertThat(exists).isTrue();

        boolean notExists = medicalRecordRepository.existsByAppointment_Id(UUID.randomUUID());
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find medical records by patient ID paginated")
    void findByPatient_Id_Success() {
        Page<MedicalRecord> page = medicalRecordRepository.findByPatient_Id(patient.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(medicalRecord.getId());
    }

    @Test
    @DisplayName("Should find medical records by doctor ID paginated")
    void findByDoctor_Id_Success() {
        Page<MedicalRecord> page = medicalRecordRepository.findByDoctor_Id(doctor.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(medicalRecord.getId());
    }
}
