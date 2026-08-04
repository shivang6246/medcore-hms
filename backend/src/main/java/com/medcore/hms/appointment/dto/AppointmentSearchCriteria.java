package com.medcore.hms.appointment.dto;

import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;

import java.time.LocalDate;
import java.util.UUID;

public record AppointmentSearchCriteria(
        UUID hospitalId,
        UUID patientId,
        UUID doctorId,
        AppointmentStatus status,
        AppointmentType type,
        LocalDate fromDate,
        LocalDate toDate
) {}
