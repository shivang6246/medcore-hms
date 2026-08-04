package com.medcore.hms.doctor.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorAvailabilityDto(
        UUID doctorId,
        DayOfWeek dayOfWeek,
        LocalTime slotStart,
        LocalTime slotEnd,
        boolean isBookable
) {}
