package com.medcore.hms.telemedicine.mapper;

import com.medcore.hms.telemedicine.dto.TelemedicineSessionResponseDto;
import com.medcore.hms.telemedicine.dto.TelemedicineSessionSummaryDto;
import com.medcore.hms.telemedicine.entity.TelemedicineSession;
import org.springframework.stereotype.Component;

@Component
public class TelemedicineMapper {

    public TelemedicineSessionResponseDto toResponseDto(TelemedicineSession s) {
        if (s == null) return null;

        String doctorName = (s.getDoctor() != null && s.getDoctor().getUser() != null)
                ? s.getDoctor().getUser().getFirstName() + " " + s.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        String patientName = s.getPatient() != null
                ? s.getPatient().getFirstName() + " " + s.getPatient().getLastName()
                : "Unknown Patient";

        return new TelemedicineSessionResponseDto(
                s.getId(),
                s.getRoomCode(),
                s.getMeetingUrl(),
                s.getAppointment() != null ? s.getAppointment().getId() : null,
                s.getDoctor() != null ? s.getDoctor().getId() : null,
                doctorName,
                s.getPatient() != null ? s.getPatient().getId() : null,
                patientName,
                s.getScheduledStartTime(),
                s.getActualStartTime(),
                s.getEndTime(),
                s.getStatus(),
                s.getSummaryNotes(),
                s.getCreatedAt()
        );
    }

    public TelemedicineSessionSummaryDto toSummaryDto(TelemedicineSession s) {
        if (s == null) return null;

        String doctorName = (s.getDoctor() != null && s.getDoctor().getUser() != null)
                ? s.getDoctor().getUser().getFirstName() + " " + s.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        String patientName = s.getPatient() != null
                ? s.getPatient().getFirstName() + " " + s.getPatient().getLastName()
                : "Unknown Patient";

        return new TelemedicineSessionSummaryDto(
                s.getId(),
                s.getRoomCode(),
                patientName,
                doctorName,
                s.getScheduledStartTime(),
                s.getStatus()
        );
    }
}
