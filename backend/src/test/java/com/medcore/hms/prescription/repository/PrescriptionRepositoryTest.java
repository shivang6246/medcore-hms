package com.medcore.hms.prescription.repository;

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
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.prescription.entity.Prescription;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("PrescriptionRepository — Integration Tests")
class PrescriptionRepositoryTest {

    @Autowired
    private PrescriptionRepository prescriptionRepository;
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

    private MedicalRecord medicalRecord;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("General Hospital")
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("genhosp_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        Department department = departmentRepository.save(Department.builder()
                .name("Internal Medicine " + UUID.randomUUID().toString().substring(0, 5))
                .hospital(hospital)
                .isActive(true)
                .build());

        Role doctorRole = roleRepository.findByName(RoleName.DOCTOR)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.DOCTOR).description("Doctor").build()));

        User doctorUser = userRepository.save(User.builder()
                .firstName("John")
                .lastName("Watson")
                .email("watson_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .passwordHash("password123")
                .roles(Set.of(doctorRole))
                .hospital(hospital)
                .isActive(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(doctorUser)
                .hospital(hospital)
                .department(department)
                .employeeId("EMP-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("DOCLIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email(doctorUser.getEmail())
                .specialization("General Medicine")
                .isActive(true)
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Sherlock")
                .lastName("Holmes")
                .dateOfBirth(LocalDate.of(1980, 1, 6))
                .phone("98765432" + (int)(Math.random()*100))
                .email("sherlock_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("John Watson")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        DoctorSlot slot = doctorSlotRepository.save(DoctorSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now())
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .status(SlotStatus.BOOKED)
                .build());

        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .appointmentNumber("APT-" + UUID.randomUUID().toString().substring(0, 8))
                .hospital(hospital)
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(11, 30))
                .status(AppointmentStatus.COMPLETED)
                .build());

        medicalRecord = medicalRecordRepository.save(MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .symptoms("Cough and fever")
                .diagnosis("Bronchitis")
                .treatmentPlan("Antibiotics course")
                .isActive(true)
                .build());

        prescription = prescriptionRepository.save(Prescription.builder()
                .medicalRecord(medicalRecord)
                .medicineName("Amoxicillin")
                .dosage("500mg")
                .frequency("3 times daily")
                .duration(7)
                .instructions("Take after meal")
                .quantity(21)
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Should find prescriptions by Medical Record ID paginated")
    void findByMedicalRecord_Id_Paginated_Success() {
        Page<Prescription> page = prescriptionRepository.findByMedicalRecord_Id(medicalRecord.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getMedicineName()).isEqualTo("Amoxicillin");
    }

    @Test
    @DisplayName("Should find list of prescriptions by Medical Record ID")
    void findByMedicalRecord_Id_List_Success() {
        List<Prescription> list = prescriptionRepository.findByMedicalRecord_Id(medicalRecord.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getDosage()).isEqualTo("500mg");
    }
}
