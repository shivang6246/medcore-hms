package com.medcore.hms.telemedicine.service;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.telemedicine.dto.*;
import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import com.medcore.hms.telemedicine.entity.TelemedicineSession;
import com.medcore.hms.telemedicine.exception.InvalidSessionStateException;
import com.medcore.hms.telemedicine.exception.TelemedicineSessionNotFoundException;
import com.medcore.hms.telemedicine.mapper.TelemedicineMapper;
import com.medcore.hms.telemedicine.repository.TelemedicineSessionRepository;
import com.medcore.hms.telemedicine.service.impl.TelemedicineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelemedicineService Unit Tests")
class TelemedicineServiceImplTest {

    @Mock private TelemedicineSessionRepository sessionRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private TelemedicineMapper telemedicineMapper;

    @InjectMocks
    private TelemedicineServiceImpl telemedicineService;

    private UUID sessionId;
    private UUID appointmentId;
    private Appointment appointment;
    private TelemedicineSession session;
    private CreateTelemedicineSessionRequestDto createDto;
    private TelemedicineSessionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        Doctor doctor = Doctor.builder().build();
        doctor.setId(UUID.randomUUID());

        Patient patient = Patient.builder().firstName("John").lastName("Doe").build();
        patient.setId(UUID.randomUUID());

        appointment = Appointment.builder().doctor(doctor).patient(patient).build();
        appointment.setId(appointmentId);

        session = TelemedicineSession.builder()
                .roomCode("ROOM-TEST-100")
                .meetingUrl("https://telehealth.medcore.hms/meet/ROOM-TEST-100")
                .appointment(appointment)
                .doctor(doctor)
                .patient(patient)
                .scheduledStartTime(LocalDateTime.now())
                .status(ConsultationSessionStatus.WAITING_ROOM)
                .doctorToken("DOC-TOK-1")
                .patientToken("PAT-TOK-1")
                .build();
        session.setId(sessionId);

        createDto = new CreateTelemedicineSessionRequestDto(appointmentId, LocalDateTime.now());

        responseDto = new TelemedicineSessionResponseDto(
                sessionId, "ROOM-TEST-100", "https://telehealth.medcore.hms/meet/ROOM-TEST-100",
                appointmentId, doctor.getId(), "Dr. Smith", patient.getId(), "John Doe",
                LocalDateTime.now(), null, null, ConsultationSessionStatus.WAITING_ROOM, null, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Create Telemedicine Session Tests")
    class CreateSessionTests {

        @Test
        @DisplayName("Should successfully create video session")
        void createSession_Success() {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(sessionRepository.save(any(TelemedicineSession.class))).thenReturn(session);
            when(telemedicineMapper.toResponseDto(session)).thenReturn(responseDto);

            TelemedicineSessionResponseDto result = telemedicineService.createSession(createDto);

            assertThat(result).isNotNull();
            assertThat(result.roomCode()).isEqualTo("ROOM-TEST-100");
            verify(sessionRepository).save(any(TelemedicineSession.class));
        }

        @Test
        @DisplayName("Should throw AppointmentNotFoundException when appointment missing")
        void createSession_AppointmentNotFound() {
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> telemedicineService.createSession(createDto))
                    .isInstanceOf(AppointmentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Join & State Transition Tests")
    class SessionStateTests {

        @Test
        @DisplayName("Should return join credentials for patient")
        void joinWaitingRoom_PatientSuccess() {
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

            JoinSessionResponseDto result = telemedicineService.joinWaitingRoom(sessionId, "PATIENT");

            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo("PATIENT");
            assertThat(result.token()).isEqualTo("PAT-TOK-1");
        }

        @Test
        @DisplayName("Should start consultation and update status to IN_PROGRESS")
        void startConsultation_Success() {
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(sessionRepository.save(any(TelemedicineSession.class))).thenReturn(session);
            when(telemedicineMapper.toResponseDto(session)).thenReturn(responseDto);

            TelemedicineSessionResponseDto result = telemedicineService.startConsultation(sessionId);

            assertThat(result).isNotNull();
            assertThat(session.getStatus()).isEqualTo(ConsultationSessionStatus.IN_PROGRESS);
            assertThat(session.getActualStartTime()).isNotNull();
        }

        @Test
        @DisplayName("Should complete consultation and set end time")
        void completeConsultation_Success() {
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
            when(sessionRepository.save(any(TelemedicineSession.class))).thenReturn(session);
            when(telemedicineMapper.toResponseDto(session)).thenReturn(responseDto);

            TelemedicineSessionResponseDto result = telemedicineService.completeConsultation(sessionId, "Notes");

            assertThat(result).isNotNull();
            assertThat(session.getStatus()).isEqualTo(ConsultationSessionStatus.COMPLETED);
            assertThat(session.getEndTime()).isNotNull();
        }

        @Test
        @DisplayName("Should throw InvalidSessionStateException when joining COMPLETED session")
        void joinWaitingRoom_CompletedError() {
            session.setStatus(ConsultationSessionStatus.COMPLETED);

            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

            assertThatThrownBy(() -> telemedicineService.joinWaitingRoom(sessionId, "PATIENT"))
                    .isInstanceOf(InvalidSessionStateException.class);
        }
    }
}
