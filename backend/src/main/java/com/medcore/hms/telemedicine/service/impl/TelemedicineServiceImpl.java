package com.medcore.hms.telemedicine.service.impl;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.telemedicine.dto.*;
import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import com.medcore.hms.telemedicine.entity.TelemedicineSession;
import com.medcore.hms.telemedicine.exception.InvalidSessionStateException;
import com.medcore.hms.telemedicine.exception.TelemedicineSessionNotFoundException;
import com.medcore.hms.telemedicine.mapper.TelemedicineMapper;
import com.medcore.hms.telemedicine.repository.TelemedicineSessionRepository;
import com.medcore.hms.telemedicine.service.TelemedicineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TelemedicineServiceImpl implements TelemedicineService {

    private final TelemedicineSessionRepository sessionRepository;
    private final AppointmentRepository appointmentRepository;
    private final TelemedicineMapper telemedicineMapper;

    @Override
    @Transactional
    public TelemedicineSessionResponseDto createSession(CreateTelemedicineSessionRequestDto dto) {
        log.info("Creating telemedicine video session for appointment ID: {}", dto.appointmentId());

        Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(dto.appointmentId()));

        String roomCode = "ROOM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String meetingUrl = "https://telehealth.medcore.hms/meet/" + roomCode;
        String docToken = "DOC-TOK-" + UUID.randomUUID().toString().substring(0, 12);
        String patToken = "PAT-TOK-" + UUID.randomUUID().toString().substring(0, 12);

        TelemedicineSession session = TelemedicineSession.builder()
                .roomCode(roomCode)
                .meetingUrl(meetingUrl)
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .patient(appointment.getPatient())
                .scheduledStartTime(dto.scheduledStartTime() != null ? dto.scheduledStartTime() : LocalDateTime.now())
                .status(ConsultationSessionStatus.WAITING_ROOM)
                .doctorToken(docToken)
                .patientToken(patToken)
                .build();

        TelemedicineSession savedSession = sessionRepository.save(session);
        log.info("Telemedicine session created with room code: {}", roomCode);
        return telemedicineMapper.toResponseDto(savedSession);
    }

    @Override
    public TelemedicineSessionResponseDto getSessionById(UUID id) {
        log.info("Fetching telemedicine session by ID: {}", id);

        TelemedicineSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new TelemedicineSessionNotFoundException(id));

        return telemedicineMapper.toResponseDto(session);
    }

    @Override
    @Transactional
    public JoinSessionResponseDto joinWaitingRoom(UUID id, String role) {
        log.info("Participant joining telemedicine room session ID: {}, role: {}", id, role);

        TelemedicineSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new TelemedicineSessionNotFoundException(id));

        if (session.getStatus() == ConsultationSessionStatus.COMPLETED || session.getStatus() == ConsultationSessionStatus.CANCELLED) {
            throw new InvalidSessionStateException("Cannot join consultation session in state: " + session.getStatus());
        }

        String token = "DOCTOR".equalsIgnoreCase(role) ? session.getDoctorToken() : session.getPatientToken();

        return new JoinSessionResponseDto(
                session.getId(),
                session.getRoomCode(),
                session.getMeetingUrl(),
                role != null ? role.toUpperCase() : "PATIENT",
                token
        );
    }

    @Override
    @Transactional
    public TelemedicineSessionResponseDto startConsultation(UUID id) {
        log.info("Doctor starting telemedicine video session ID: {}", id);

        TelemedicineSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new TelemedicineSessionNotFoundException(id));

        session.setStatus(ConsultationSessionStatus.IN_PROGRESS);
        session.setActualStartTime(LocalDateTime.now());

        TelemedicineSession updatedSession = sessionRepository.save(session);
        log.info("Telemedicine session ID: {} is now IN_PROGRESS", id);
        return telemedicineMapper.toResponseDto(updatedSession);
    }

    @Override
    @Transactional
    public TelemedicineSessionResponseDto completeConsultation(UUID id, String notes) {
        log.info("Completing telemedicine video session ID: {}", id);

        TelemedicineSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new TelemedicineSessionNotFoundException(id));

        session.setStatus(ConsultationSessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        if (notes != null && !notes.isBlank()) {
            session.setSummaryNotes(notes);
        }

        TelemedicineSession updatedSession = sessionRepository.save(session);
        log.info("Telemedicine session ID: {} completed successfully", id);
        return telemedicineMapper.toResponseDto(updatedSession);
    }

    @Override
    public List<TelemedicineSessionSummaryDto> getDoctorWaitingRoomQueue(UUID doctorId) {
        log.info("Fetching virtual waiting room queue for doctor ID: {}", doctorId);

        List<TelemedicineSession> sessions = sessionRepository.findByDoctor_IdAndStatus(
                doctorId, ConsultationSessionStatus.WAITING_ROOM
        );

        return sessions.stream()
                .map(telemedicineMapper::toSummaryDto)
                .toList();
    }

    @Override
    public PagedResponse<TelemedicineSessionSummaryDto> getPatientConsultationHistory(UUID patientId, Pageable pageable) {
        log.info("Fetching consultation history for patient ID: {}", patientId);

        Page<TelemedicineSession> page = sessionRepository.findByPatient_Id(patientId, pageable);
        return PagedResponse.from(page.map(telemedicineMapper::toSummaryDto));
    }
}
