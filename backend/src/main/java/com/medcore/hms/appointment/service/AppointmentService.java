package com.medcore.hms.appointment.service;

import com.medcore.hms.appointment.dto.*;
import com.medcore.hms.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponseDto bookAppointment(BookAppointmentRequestDto dto);

    AppointmentResponseDto rescheduleAppointment(UUID id, RescheduleAppointmentRequestDto dto);

    AppointmentResponseDto cancelAppointment(UUID id, CancelAppointmentRequestDto dto);

    AppointmentResponseDto confirmAppointment(UUID id);

    AppointmentResponseDto checkInPatient(UUID id);

    AppointmentResponseDto markInProgress(UUID id);

    AppointmentResponseDto completeAppointment(UUID id);

    AppointmentResponseDto markNoShow(UUID id);

    AppointmentResponseDto updateNotes(UUID id, UpdateAppointmentNotesRequestDto dto);

    AppointmentResponseDto getAppointmentById(UUID id);

    PagedResponse<AppointmentSummaryDto> getAppointmentsByDoctor(UUID doctorId, Pageable pageable);

    PagedResponse<AppointmentSummaryDto> getAppointmentsByPatient(UUID patientId, Pageable pageable);

    PagedResponse<AppointmentSummaryDto> getAppointmentsByHospital(UUID hospitalId, Pageable pageable);

    PagedResponse<AppointmentSummaryDto> searchAppointments(AppointmentSearchCriteria criteria, Pageable pageable);

    DailyScheduleDto getDoctorDailySchedule(UUID doctorId, LocalDate date);
}
