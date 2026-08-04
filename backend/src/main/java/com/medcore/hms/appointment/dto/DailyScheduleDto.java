package com.medcore.hms.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record DailyScheduleDto(
        UUID doctorId,
        String doctorName,
        String specialization,
        LocalDate date,
        int totalSlots,
        int bookedSlots,
        int availableSlots,
        List<SlotItem> schedule
) {
    public record SlotItem(
            UUID slotId,
            LocalTime startTime,
            LocalTime endTime,
            String slotStatus,
            UUID appointmentId,
            String appointmentNumber,
            String patientName,
            String patientId,
            String appointmentStatus,
            String chiefComplaint
    ) {}
}
