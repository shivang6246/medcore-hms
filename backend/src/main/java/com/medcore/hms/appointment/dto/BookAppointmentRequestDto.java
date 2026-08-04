package com.medcore.hms.appointment.dto;

import com.medcore.hms.appointment.entity.AppointmentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookAppointmentRequestDto(

        @NotNull(message = "Patient ID is required")
        UUID patientId,

        @NotNull(message = "Doctor ID is required")
        UUID doctorId,

        @NotNull(message = "Slot ID is required")
        UUID slotId,

        AppointmentType type,

        @Size(max = 500)
        String chiefComplaint,

        String notes
) {}
