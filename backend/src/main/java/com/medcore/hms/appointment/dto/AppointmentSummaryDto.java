package com.medcore.hms.appointment.dto;

import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentSummaryDto(
        UUID id,
        String appointmentNumber,
        String patientName,
        UUID doctorId,
        String doctorName,
        String doctorEmployeeId,
        String hospitalName,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        AppointmentType type
) {}
