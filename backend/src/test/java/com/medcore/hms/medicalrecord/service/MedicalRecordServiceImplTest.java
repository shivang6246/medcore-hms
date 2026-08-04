package com.medcore.hms.medicalrecord.service;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.medicalrecord.exception.AppointmentMismatchException;
import com.medcore.hms.medicalrecord.exception.DuplicateMedicalRecordException;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.medicalrecord.mapper.MedicalRecordMapper;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.medicalrecord.service.impl.MedicalRecordServiceImpl;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicalRecordService Unit Tests")
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;
    private UUID recordId;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;
    private MedicalRecord record;
    private CreateMedicalRecordRequestDto createDto;
    private MedicalRecordResponseDto responseDto;

    @BeforeEach
    void setUp() {
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        recordId = UUID.randomUUID();

        patient = Patient.builder().firstName("John").lastName("Doe").build();
        patient.setId(patientId);

        User doctorUser = User.builder().firstName("Alice").lastName("Smith").build();
        doctor = Doctor.builder().user(doctorUser).specialization("Cardiology").build();
        doctor.setId(doctorId);

        appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentNumber("APT-1001")
                .appointmentDate(LocalDate.now())
                .build();
        appointment.setId(appointmentId);

        record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .symptoms("Chest pain")
                .diagnosis("Angina Pectoris")
                .treatmentPlan("Rest and sublingual nitroglycerin")
                .notes("Regular follow-up in 2 weeks")
                .followUpDate(LocalDate.now().plusDays(14))
                .isActive(true)
                .build();
        record.setId(recordId);

        createDto = new CreateMedicalRecordRequestDto(
                patientId, doctorId, appointmentId,
                "Chest pain", "Angina Pectoris",
                "Rest and sublingual nitroglycerin",
                "Regular follow-up in 2 weeks",
                LocalDate.now().plusDays(14)
        );

        responseDto = new MedicalRecordResponseDto(
                recordId,
                new MedicalRecordResponseDto.PatientRefDto(patientId, "P-100", "John", "Doe", "1234567890"),
                new MedicalRecordResponseDto.DoctorRefDto(doctorId, "Alice", "Smith", "Cardiology"),
                new MedicalRecordResponseDto.AppointmentRefDto(appointmentId, "APT-1001", LocalDate.now()),
                "Chest pain", "Angina Pectoris",
                "Rest and sublingual nitroglycerin",
                "Regular follow-up in 2 weeks",
                LocalDate.now().plusDays(14),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Create Medical Record Tests")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create medical record when all inputs are valid")
        void createMedicalRecord_Success() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(medicalRecordRepository.existsByAppointment_Id(appointmentId)).thenReturn(false);
            when(medicalRecordMapper.toEntity(any(), any(), any(), any())).thenReturn(record);
            when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);
            when(medicalRecordMapper.toResponseDto(record)).thenReturn(responseDto);

            MedicalRecordResponseDto result = medicalRecordService.createMedicalRecord(createDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(recordId);
            verify(medicalRecordRepository).save(any(MedicalRecord.class));
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException if patient does not exist")
        void createMedicalRecord_PatientNotFound() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(createDto))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw DoctorNotFoundException if doctor does not exist")
        void createMedicalRecord_DoctorNotFound() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(createDto))
                    .isInstanceOf(DoctorNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AppointmentNotFoundException if appointment does not exist")
        void createMedicalRecord_AppointmentNotFound() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(createDto))
                    .isInstanceOf(AppointmentNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AppointmentMismatchException if appointment patient does not match")
        void createMedicalRecord_PatientMismatch() {
            Patient otherPatient = Patient.builder().build();
            otherPatient.setId(UUID.randomUUID());
            Appointment mismatchAppt = Appointment.builder().patient(otherPatient).doctor(doctor).build();
            mismatchAppt.setId(appointmentId);

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(mismatchAppt));

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(createDto))
                    .isInstanceOf(AppointmentMismatchException.class)
                    .hasMessageContaining("does not belong to patient");
        }

        @Test
        @DisplayName("Should throw DuplicateMedicalRecordException if record already exists for appointment")
        void createMedicalRecord_DuplicateRecord() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(medicalRecordRepository.existsByAppointment_Id(appointmentId)).thenReturn(true);

            assertThatThrownBy(() -> medicalRecordService.createMedicalRecord(createDto))
                    .isInstanceOf(DuplicateMedicalRecordException.class);
        }
    }

    @Nested
    @DisplayName("Fetch & List Tests")
    class FetchTests {

        @Test
        @DisplayName("Should return record by ID")
        void getMedicalRecordById_Success() {
            when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(medicalRecordMapper.toResponseDto(record)).thenReturn(responseDto);

            MedicalRecordResponseDto result = medicalRecordService.getMedicalRecordById(recordId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(recordId);
        }

        @Test
        @DisplayName("Should throw MedicalRecordNotFoundException when ID invalid")
        void getMedicalRecordById_NotFound() {
            when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medicalRecordService.getMedicalRecordById(recordId))
                    .isInstanceOf(MedicalRecordNotFoundException.class);
        }

        @Test
        @DisplayName("Should list records by Patient paginated")
        void getMedicalRecordsByPatient_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<MedicalRecord> page = new PageImpl<>(List.of(record));
            MedicalRecordSummaryDto summaryDto = new MedicalRecordSummaryDto(
                    recordId, "John Doe", "Alice Smith", "APT-1001", "Angina Pectoris", LocalDate.now(), true, LocalDateTime.now()
            );

            when(patientRepository.existsById(patientId)).thenReturn(true);
            when(medicalRecordRepository.findByPatient_Id(patientId, pageable)).thenReturn(page);
            when(medicalRecordMapper.toSummaryDto(record)).thenReturn(summaryDto);

            PagedResponse<MedicalRecordSummaryDto> response = medicalRecordService.getMedicalRecordsByPatient(patientId, pageable);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Update & Deactivate Tests")
    class ModifyTests {

        @Test
        @DisplayName("Should update medical record successfully")
        void updateMedicalRecord_Success() {
            UpdateMedicalRecordRequestDto updateDto = new UpdateMedicalRecordRequestDto(
                    "Mild headache", "Recovered", "No medication", "Discharged", LocalDate.now()
            );

            when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(medicalRecordRepository.save(record)).thenReturn(record);
            when(medicalRecordMapper.toResponseDto(record)).thenReturn(responseDto);

            MedicalRecordResponseDto result = medicalRecordService.updateMedicalRecord(recordId, updateDto);

            assertThat(result).isNotNull();
            verify(medicalRecordMapper).updateEntity(eq(record), eq(updateDto));
            verify(medicalRecordRepository).save(record);
        }

        @Test
        @DisplayName("Should deactivate medical record")
        void deactivateMedicalRecord_Success() {
            when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
            when(medicalRecordRepository.save(record)).thenReturn(record);
            when(medicalRecordMapper.toResponseDto(record)).thenReturn(responseDto);

            MedicalRecordResponseDto result = medicalRecordService.deactivateMedicalRecord(recordId);

            assertThat(result).isNotNull();
            assertThat(record.isActive()).isFalse();
            verify(medicalRecordRepository).save(record);
        }
    }
}
