package com.medcore.hms.appointment.dto;

import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentResponseDto(
        UUID id,
        String appointmentNumber,
        UUID hospitalId,
        String hospitalName,
        PatientRefDto patient,
        DoctorRefDto doctor,
        SlotRefDto slot,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        AppointmentType type,
        String chiefComplaint,
        String notes,
        String cancelReason,
        BigDecimal consultationFee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record PatientRefDto(UUID id, String patientId, String firstName, String lastName, String phone) {}
    public record DoctorRefDto(UUID id, String firstName, String lastName, String specialization) {}
    public record SlotRefDto(UUID id, LocalDate slotDate, LocalTime startTime, LocalTime endTime) {}
}
