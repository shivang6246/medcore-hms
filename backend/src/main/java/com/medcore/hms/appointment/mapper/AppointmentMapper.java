package com.medcore.hms.appointment.mapper;

import com.medcore.hms.appointment.dto.AppointmentResponseDto;
import com.medcore.hms.appointment.dto.AppointmentSummaryDto;
import com.medcore.hms.appointment.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toResponseDto(Appointment a) {
        return new AppointmentResponseDto(
                a.getId(),
                a.getAppointmentNumber(),
                a.getHospital().getId(),
                a.getHospital().getName(),
                new AppointmentResponseDto.PatientRefDto(
                        a.getPatient().getId(),
                        a.getPatient().getPatientId(),
                        a.getPatient().getFirstName(),
                        a.getPatient().getLastName(),
                        a.getPatient().getPhone()
                ),
                new AppointmentResponseDto.DoctorRefDto(
                        a.getDoctor().getId(),
                        a.getDoctor().getUser().getFirstName(),
                        a.getDoctor().getUser().getLastName(),
                        a.getDoctor().getSpecialization()
                ),
                new AppointmentResponseDto.SlotRefDto(
                        a.getSlot().getId(),
                        a.getSlot().getSlotDate(),
                        a.getSlot().getStartTime(),
                        a.getSlot().getEndTime()
                ),
                a.getAppointmentDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getType(),
                a.getChiefComplaint(),
                a.getNotes(),
                a.getCancelReason(),
                a.getConsultationFee(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    public AppointmentSummaryDto toSummaryDto(Appointment a) {
        String patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();
        String doctorName  = a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName();
        return new AppointmentSummaryDto(
                a.getId(),
                a.getAppointmentNumber(),
                patientName,
                a.getDoctor().getId(),
                doctorName,
                a.getDoctor().getEmployeeId(),
                a.getHospital().getName(),
                a.getAppointmentDate(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus(),
                a.getType()
        );
    }
}
