package com.medcore.hms.telemedicine.repository;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import com.medcore.hms.telemedicine.entity.TelemedicineSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Telemedicine Repository Integration Tests")
class TelemedicineRepositoryTest {

    @Autowired private TelemedicineSessionRepository sessionRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private com.medcore.hms.user.repository.UserRepository userRepository;
    @Autowired private com.medcore.hms.department.repository.DepartmentRepository departmentRepository;
    @Autowired private com.medcore.hms.doctor.slot.repository.DoctorSlotRepository doctorSlotRepository;

    private Doctor doctor;
    private Patient patient;
    private Appointment appointment;
    private TelemedicineSession session;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("Telehealth Hospital " + UUID.randomUUID().toString().substring(0, 5))
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("telehealth_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        com.medcore.hms.department.entity.Department department = departmentRepository.save(
                com.medcore.hms.department.entity.Department.builder()
                        .name("Telehealth Department " + UUID.randomUUID().toString().substring(0, 5))
                        .hospital(hospital)
                        .isActive(true)
                        .build()
        );

        com.medcore.hms.user.entity.User user = userRepository.save(
                com.medcore.hms.user.entity.User.builder()
                        .firstName("Tele")
                        .lastName("Doc")
                        .email("teledoc_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                        .passwordHash("hashed")
                        .isActive(true)
                        .build()
        );

        patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Diana")
                .lastName("Prince")
                .dateOfBirth(LocalDate.of(1990, 3, 22))
                .phone("98765431" + (int)(Math.random()*100))
                .email("diana_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("Steve Trevor")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .user(user)
                .hospital(hospital)
                .department(department)
                .employeeId("EMP-" + UUID.randomUUID().toString().substring(0, 8))
                .email(user.getEmail())
                .licenseNumber("DOC-" + UUID.randomUUID().toString().substring(0, 8))
                .specialization("Telemedicine Specialist")
                .consultationFee(new BigDecimal("80.00"))
                .yearsOfExperience(8)
                .isActive(true)
                .build());

        com.medcore.hms.doctor.slot.entity.DoctorSlot slot = doctorSlotRepository.save(
                com.medcore.hms.doctor.slot.entity.DoctorSlot.builder()
                        .doctor(doctor)
                        .slotDate(LocalDate.now())
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(10, 30))
                        .status(com.medcore.hms.doctor.slot.entity.SlotStatus.BOOKED)
                        .build()
        );

        appointment = appointmentRepository.save(Appointment.builder()
                .appointmentNumber("APT-TELE-" + UUID.randomUUID().toString().substring(0, 5))
                .hospital(hospital)
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .consultationFee(new BigDecimal("80.00"))
                .status(AppointmentStatus.CONFIRMED)
                .type(AppointmentType.TELECONSULTATION)
                .build());

        session = sessionRepository.save(TelemedicineSession.builder()
                .roomCode("ROOM-TELE-001")
                .meetingUrl("https://telehealth.medcore.hms/meet/ROOM-TELE-001")
                .appointment(appointment)
                .doctor(doctor)
                .patient(patient)
                .scheduledStartTime(LocalDateTime.now())
                .status(ConsultationSessionStatus.WAITING_ROOM)
                .doctorToken("DOC-TOK-99")
                .patientToken("PAT-TOK-99")
                .build());
    }

    @Test
    @DisplayName("Should find session by appointment ID")
    void findByAppointment_Id_Success() {
        Optional<TelemedicineSession> found = sessionRepository.findByAppointment_Id(appointment.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoomCode()).isEqualTo("ROOM-TELE-001");
    }

    @Test
    @DisplayName("Should find sessions in doctor's waiting room queue")
    void findByDoctor_IdAndStatus_Success() {
        List<TelemedicineSession> waitingQueue = sessionRepository.findByDoctor_IdAndStatus(
                doctor.getId(), ConsultationSessionStatus.WAITING_ROOM
        );

        assertThat(waitingQueue).hasSize(1);
        assertThat(waitingQueue.get(0).getRoomCode()).isEqualTo("ROOM-TELE-001");
    }
}
