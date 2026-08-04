package com.medcore.hms.appointment.service;

import com.medcore.hms.appointment.dto.*;
import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;
import com.medcore.hms.appointment.exception.*;
import com.medcore.hms.appointment.mapper.AppointmentMapper;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.appointment.service.impl.AppointmentServiceImpl;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.doctor.slot.entity.SlotStatus;
import com.medcore.hms.doctor.slot.repository.DoctorSlotRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.patient.entity.Patient;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService Unit Tests")
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository     patientRepository;
    @Mock private DoctorRepository      doctorRepository;
    @Mock private DoctorSlotRepository  slotRepository;
    @Mock private AppointmentMapper     appointmentMapper;

    @InjectMocks private AppointmentServiceImpl appointmentService;

    private UUID apptId, patientId, doctorId, slotId, newSlotId, hospitalId;
    private Hospital hospital;
    private Patient  patient;
    private Doctor   doctor;
    private DoctorSlot slot, newSlot;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        apptId    = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId  = UUID.randomUUID();
        slotId    = UUID.randomUUID();
        newSlotId = UUID.randomUUID();
        hospitalId = UUID.randomUUID();

        hospital = Hospital.builder()
                .name("Test Hospital")
                .registrationNumber("R-001")
                .licenseNumber("L-001")
                .email("h@test.com")
                .isActive(true)
                .build();
        // Reflectively set the inherited UUID so getId() returns a value in tests
        setId(hospital, hospitalId);

        User user = User.builder()
                .firstName("Arjun")
                .lastName("Sharma")
                .email("dr@test.com")
                .passwordHash("hash")
                .isActive(true)
                .isEmailVerified(true)
                .build();

        patient = Patient.builder()
                .hospital(hospital)
                .patientId("P-2026-00001")
                .firstName("Aanya")
                .lastName("Mehta")
                .dateOfBirth(LocalDate.of(1992, 5, 14))
                .phone("+91-9811223344")
                .emergencyContactName("Rajiv")
                .emergencyContactPhone("+91-9822334455")
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .employeeId("EMP-001")
                .email("dr@test.com")
                .licenseNumber("LIC-001")
                .specialization("Cardiology")
                .consultationFee(new BigDecimal("500.00"))
                .isActive(true)
                .isAvailable(true)
                .build();
        setId(doctor, doctorId);

        slot = DoctorSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        newSlot = DoctorSlot.builder()
                .doctor(doctor)
                .slotDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(10, 30))
                .status(SlotStatus.AVAILABLE)
                .build();

        appointment = Appointment.builder()
                .appointmentNumber("APT-20260729-ABCD")
                .hospital(hospital)
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(AppointmentStatus.SCHEDULED)
                .type(AppointmentType.IN_PERSON)
                .consultationFee(new BigDecimal("500.00"))
                .build();
    }

    @Nested
    @DisplayName("bookAppointment")
    class BookTests {

        @Test
        @DisplayName("should book successfully and set slot to BOOKED")
        void bookAppointment_success() {
            BookAppointmentRequestDto dto = new BookAppointmentRequestDto(
                    patientId, doctorId, slotId, AppointmentType.IN_PERSON, "Chest pain", null);

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
            when(appointmentRepository.save(any())).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            AppointmentResponseDto result = appointmentService.bookAppointment(dto);

            assertThat(result).isNotNull();
            assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
            verify(slotRepository).save(slot);
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("should throw SlotNotAvailableException when slot is already BOOKED")
        void bookAppointment_slotAlreadyBooked_throws() {
            slot.setStatus(SlotStatus.BOOKED);
            BookAppointmentRequestDto dto = new BookAppointmentRequestDto(
                    patientId, doctorId, slotId, null, null, null);

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

            assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                    .isInstanceOf(SlotNotAvailableException.class);

            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw SlotNotAvailableException when slot is BLOCKED")
        void bookAppointment_slotBlocked_throws() {
            slot.setStatus(SlotStatus.BLOCKED);
            BookAppointmentRequestDto dto = new BookAppointmentRequestDto(
                    patientId, doctorId, slotId, null, null, null);

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

            assertThatThrownBy(() -> appointmentService.bookAppointment(dto))
                    .isInstanceOf(SlotNotAvailableException.class);
        }
    }

    @Nested
    @DisplayName("cancelAppointment")
    class CancelTests {

        @Test
        @DisplayName("should cancel and release slot back to AVAILABLE")
        void cancelAppointment_releasesSlot() {
            slot.setStatus(SlotStatus.BOOKED);
            CancelAppointmentRequestDto dto = new CancelAppointmentRequestDto("Patient request");

            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            appointmentService.cancelAppointment(apptId, dto);

            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
            assertThat(appointment.getCancelReason()).isEqualTo("Patient request");
            verify(slotRepository).save(slot);
        }

        @Test
        @DisplayName("should throw AppointmentStatusException when cancelling COMPLETED appointment")
        void cancel_completedAppointment_throws() {
            appointment.setStatus(AppointmentStatus.COMPLETED);

            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.cancelAppointment(apptId, null))
                    .isInstanceOf(AppointmentStatusException.class)
                    .hasMessageContaining("COMPLETED");
        }
    }

    @Nested
    @DisplayName("rescheduleAppointment")
    class RescheduleTests {

        @Test
        @DisplayName("should atomically swap old and new slots")
        void reschedule_swapsSlots() {
            slot.setStatus(SlotStatus.BOOKED);
            RescheduleAppointmentRequestDto dto = new RescheduleAppointmentRequestDto(newSlotId, "Earlier slot");

            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(slotRepository.findById(newSlotId)).thenReturn(Optional.of(newSlot));
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            appointmentService.rescheduleAppointment(apptId, dto);

            assertThat(slot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
            assertThat(newSlot.getStatus()).isEqualTo(SlotStatus.BOOKED);
            assertThat(appointment.getSlot()).isEqualTo(newSlot);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        }

        @Test
        @DisplayName("should throw SlotNotAvailableException if new slot is not available")
        void reschedule_newSlotNotAvailable_throws() {
            newSlot.setStatus(SlotStatus.BOOKED);
            RescheduleAppointmentRequestDto dto = new RescheduleAppointmentRequestDto(newSlotId, null);

            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(slotRepository.findById(newSlotId)).thenReturn(Optional.of(newSlot));

            assertThatThrownBy(() -> appointmentService.rescheduleAppointment(apptId, dto))
                    .isInstanceOf(SlotNotAvailableException.class);
        }
    }

    @Nested
    @DisplayName("Status transitions")
    class TransitionTests {

        @Test
        @DisplayName("SCHEDULED → CONFIRMED")
        void confirm_fromScheduled_success() {
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            appointmentService.confirmAppointment(apptId);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @Test
        @DisplayName("CONFIRMED → CHECKED_IN")
        void checkIn_fromConfirmed_success() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            appointmentService.checkInPatient(apptId);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CHECKED_IN);
        }

        @Test
        @DisplayName("should throw AppointmentStatusException on invalid transition COMPLETED → CONFIRMED")
        void confirm_fromCompleted_throws() {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));

            assertThatThrownBy(() -> appointmentService.confirmAppointment(apptId))
                    .isInstanceOf(AppointmentStatusException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("completeAppointment from IN_PROGRESS success")
        void complete_fromInProgress_success() {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(appointment)).thenReturn(appointment);
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            appointmentService.completeAppointment(apptId);

            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("getAppointmentById")
    class GetByIdTests {

        @Test
        @DisplayName("should return appointment when found")
        void getById_found() {
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
            when(appointmentMapper.toResponseDto(appointment)).thenReturn(mockResponseDto());

            AppointmentResponseDto result = appointmentService.getAppointmentById(apptId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw AppointmentNotFoundException when not found")
        void getById_notFound() {
            when(appointmentRepository.findById(apptId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> appointmentService.getAppointmentById(apptId))
                    .isInstanceOf(AppointmentNotFoundException.class)
                    .hasMessageContaining(apptId.toString());
        }
    }

    private AppointmentResponseDto mockResponseDto() {
        return new AppointmentResponseDto(
                apptId, "APT-20260729-ABCD",
                hospitalId, "Test Hospital",
                new AppointmentResponseDto.PatientRefDto(patientId, "P-2026-00001", "Aanya", "Mehta", "+91-9811223344"),
                new AppointmentResponseDto.DoctorRefDto(doctorId, "Arjun", "Sharma", "Cardiology"),
                new AppointmentResponseDto.SlotRefDto(slotId, LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30)),
                LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(9, 30),
                AppointmentStatus.SCHEDULED, AppointmentType.IN_PERSON,
                "Chest pain", null, null,
                new BigDecimal("500.00"), null, null
        );
    }

    private void setId(Object entity, UUID id) {
        try {
            java.lang.reflect.Field f = com.medcore.hms.common.entity.BaseEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity ID in test", e);
        }
    }
}
