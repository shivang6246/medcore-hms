package com.medcore.hms.appointment.dto;

import jakarta.validation.constraints.Size;

public record CancelAppointmentRequestDto(

        @Size(max = 500)
        String reason
) {}
