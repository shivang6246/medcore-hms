package com.medcore.hms.appointment.service.impl;

import com.medcore.hms.appointment.dto.*;
import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;
import com.medcore.hms.appointment.exception.*;
import com.medcore.hms.appointment.mapper.AppointmentMapper;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.appointment.service.AppointmentService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.doctor.slot.entity.SlotStatus;
import com.medcore.hms.doctor.slot.exception.SlotNotFoundException;
import com.medcore.hms.doctor.slot.repository.DoctorSlotRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private static final Set<AppointmentStatus> CANCELLABLE_STATUSES = Set.of(
            AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED,
            AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_PROGRESS
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository     patientRepository;
    private final DoctorRepository      doctorRepository;
    private final DoctorSlotRepository  slotRepository;
    private final AppointmentMapper     appointmentMapper;

    // ── Book ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppointmentResponseDto bookAppointment(BookAppointmentRequestDto dto) {
        log.info("Booking appointment — patient: {}, doctor: {}, slot: {}",
                dto.patientId(), dto.doctorId(), dto.slotId());

        Patient   patient = findPatientOrThrow(dto.patientId());
        Doctor    doctor  = findDoctorOrThrow(dto.doctorId());
        DoctorSlot slot   = findSlotOrThrow(dto.slotId());

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            log.warn("Slot {} is not available — status: {}", dto.slotId(), slot.getStatus());
            throw new SlotNotAvailableException(dto.slotId());
        }

        if (!slot.getDoctor().getId().equals(doctor.getId())) {
            throw new AppointmentConflictException("Slot does not belong to the specified doctor");
        }

        if (!patient.getHospital().getId().equals(doctor.getHospital().getId())) {
            throw new AppointmentConflictException("Patient and doctor must belong to the same hospital");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        String appointmentNumber = generateAppointmentNumber();

        Appointment appointment = Appointment.builder()
                .appointmentNumber(appointmentNumber)
                .hospital(doctor.getHospital())
                .patient(patient)
                .doctor(doctor)
                .slot(slot)
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .status(AppointmentStatus.SCHEDULED)
                .type(dto.type() != null ? dto.type() : AppointmentType.IN_PERSON)
                .chiefComplaint(dto.chiefComplaint())
                .notes(dto.notes())
                .consultationFee(doctor.getConsultationFee())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked — number: {}, ID: {}", appointmentNumber, saved.getId());
        return appointmentMapper.toResponseDto(saved);
    }

    // ── Reschedule ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppointmentResponseDto rescheduleAppointment(UUID id, RescheduleAppointmentRequestDto dto) {
        log.info("Rescheduling appointment {} to slot {}", id, dto.newSlotId());

        Appointment appointment = findOrThrow(id);
        validateCancellable(appointment);

        DoctorSlot newSlot = findSlotOrThrow(dto.newSlotId());
        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException(dto.newSlotId());
        }
        if (!newSlot.getDoctor().getId().equals(appointment.getDoctor().getId())) {
            throw new AppointmentConflictException("New slot does not belong to the appointment's doctor");
        }

        DoctorSlot oldSlot = appointment.getSlot();
        oldSlot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(oldSlot);

        newSlot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(newSlot);

        appointment.setSlot(newSlot);
        appointment.setAppointmentDate(newSlot.getSlotDate());
        appointment.setStartTime(newSlot.getStartTime());
        appointment.setEndTime(newSlot.getEndTime());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        if (dto.reason() != null) appointment.setNotes("Rescheduled: " + dto.reason());

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} rescheduled to slot {}", id, dto.newSlotId());
        return appointmentMapper.toResponseDto(updated);
    }

    // ── Cancel ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppointmentResponseDto cancelAppointment(UUID id, CancelAppointmentRequestDto dto) {
        log.info("Cancelling appointment {}", id);

        Appointment appointment = findOrThrow(id);
        validateCancellable(appointment);

        DoctorSlot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(dto != null ? dto.reason() : null);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} cancelled", id);
        return appointmentMapper.toResponseDto(updated);
    }

    // ── Status Transitions ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AppointmentResponseDto confirmAppointment(UUID id) {
        return transition(id, AppointmentStatus.CONFIRMED,
                Set.of(AppointmentStatus.SCHEDULED));
    }

    @Override
    @Transactional
    public AppointmentResponseDto checkInPatient(UUID id) {
        return transition(id, AppointmentStatus.CHECKED_IN,
                Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED));
    }

    @Override
    @Transactional
    public AppointmentResponseDto markInProgress(UUID id) {
        return transition(id, AppointmentStatus.IN_PROGRESS,
                Set.of(AppointmentStatus.CHECKED_IN));
    }

    @Override
    @Transactional
    public AppointmentResponseDto completeAppointment(UUID id) {
        return transition(id, AppointmentStatus.COMPLETED,
                Set.of(AppointmentStatus.IN_PROGRESS, AppointmentStatus.CHECKED_IN));
    }

    @Override
    @Transactional
    public AppointmentResponseDto markNoShow(UUID id) {
        log.info("Marking appointment {} as NO_SHOW", id);
        Appointment appointment = findOrThrow(id);

        if (!Set.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED).contains(appointment.getStatus())) {
            throw new AppointmentStatusException(appointment.getStatus(), AppointmentStatus.NO_SHOW);
        }

        DoctorSlot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        appointment.setStatus(AppointmentStatus.NO_SHOW);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} marked NO_SHOW", id);
        return appointmentMapper.toResponseDto(updated);
    }

    // ── Notes ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppointmentResponseDto updateNotes(UUID id, UpdateAppointmentNotesRequestDto dto) {
        log.info("Updating notes for appointment {}", id);
        Appointment appointment = findOrThrow(id);

        if (dto.chiefComplaint() != null) appointment.setChiefComplaint(dto.chiefComplaint());
        if (dto.notes()          != null) appointment.setNotes(dto.notes());

        return appointmentMapper.toResponseDto(appointmentRepository.save(appointment));
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(UUID id) {
        log.debug("Fetching appointment {}", id);
        return appointmentMapper.toResponseDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentSummaryDto> getAppointmentsByDoctor(UUID doctorId, Pageable pageable) {
        log.debug("Fetching appointments for doctor {}", doctorId);
        return PagedResponse.from(
                appointmentRepository.findByDoctor_Id(doctorId, pageable)
                        .map(appointmentMapper::toSummaryDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentSummaryDto> getAppointmentsByPatient(UUID patientId, Pageable pageable) {
        log.debug("Fetching appointments for patient {}", patientId);
        return PagedResponse.from(
                appointmentRepository.findByPatient_Id(patientId, pageable)
                        .map(appointmentMapper::toSummaryDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentSummaryDto> getAppointmentsByHospital(UUID hospitalId, Pageable pageable) {
        log.debug("Fetching appointments for hospital {}", hospitalId);
        return PagedResponse.from(
                appointmentRepository.findByHospital_Id(hospitalId, pageable)
                        .map(appointmentMapper::toSummaryDto));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentSummaryDto> searchAppointments(AppointmentSearchCriteria c, Pageable pageable) {
        log.debug("Searching appointments — criteria: {}", c);
        return PagedResponse.from(
                appointmentRepository.search(
                        c.hospitalId(), c.patientId(), c.doctorId(),
                        c.status(), c.fromDate(), c.toDate(), pageable)
                        .map(appointmentMapper::toSummaryDto));
    }

    // ── Day 6: Daily Schedule View ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DailyScheduleDto getDoctorDailySchedule(UUID doctorId, LocalDate date) {
        log.debug("Building daily schedule for doctor {} on {}", doctorId, date);

        Doctor doctor = findDoctorOrThrow(doctorId);
        List<DoctorSlot> slots = slotRepository.findByDoctor_IdAndSlotDateOrderByStartTime(doctorId, date);
        List<Appointment> appointments = appointmentRepository
                .findByDoctor_IdAndAppointmentDateOrderByStartTime(doctorId, date);

        java.util.Map<UUID, Appointment> slotToAppt = new java.util.HashMap<>();
        for (Appointment a : appointments) {
            slotToAppt.put(a.getSlot().getId(), a);
        }

        List<DailyScheduleDto.SlotItem> items = slots.stream().map(s -> {
            Appointment a = slotToAppt.get(s.getId());
            return new DailyScheduleDto.SlotItem(
                    s.getId(),
                    s.getStartTime(),
                    s.getEndTime(),
                    s.getStatus().name(),
                    a != null ? a.getId() : null,
                    a != null ? a.getAppointmentNumber() : null,
                    a != null ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName() : null,
                    a != null ? a.getPatient().getPatientId() : null,
                    a != null ? a.getStatus().name() : null,
                    a != null ? a.getChiefComplaint() : null
            );
        }).toList();

        long booked    = slots.stream().filter(s -> s.getStatus() == SlotStatus.BOOKED).count();
        long available = slots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).count();

        return new DailyScheduleDto(
                doctor.getId(),
                doctor.getUser().getFirstName() + " " + doctor.getUser().getLastName(),
                doctor.getSpecialization(),
                date,
                slots.size(),
                (int) booked,
                (int) available,
                items
        );
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private AppointmentResponseDto transition(UUID id, AppointmentStatus target, Set<AppointmentStatus> allowed) {
        log.info("Transitioning appointment {} to {}", id, target);
        Appointment appointment = findOrThrow(id);
        if (!allowed.contains(appointment.getStatus())) {
            throw new AppointmentStatusException(appointment.getStatus(), target);
        }
        appointment.setStatus(target);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} transitioned to {}", id, target);
        return appointmentMapper.toResponseDto(updated);
    }

    private void validateCancellable(Appointment appointment) {
        if (!CANCELLABLE_STATUSES.contains(appointment.getStatus())) {
            throw new AppointmentStatusException(
                    "Cannot modify appointment in status: " + appointment.getStatus());
        }
    }

    private String generateAppointmentNumber() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "APT-" + ts + "-" + suffix;
    }

    private Appointment findOrThrow(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Appointment not found — ID: {}", id);
                    return new AppointmentNotFoundException(id);
                });
    }

    private Patient findPatientOrThrow(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    private Doctor findDoctorOrThrow(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    private DoctorSlot findSlotOrThrow(UUID id) {
        return slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException(id));
    }
}
