package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.Size;

public record UpdateAppointmentNotesRequestDto(

        @Size(max = 500)
        String chiefComplaint,

        String notes
) {}
