package com.medcore.hms.medicalrecord.mapper;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.patient.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponseDto toResponseDto(MedicalRecord record) {
        if (record == null) {
            return null;
        }

        Patient patient = record.getPatient();
        Doctor doctor = record.getDoctor();
        Appointment appointment = record.getAppointment();

        MedicalRecordResponseDto.PatientRefDto patientRef = patient != null ? new MedicalRecordResponseDto.PatientRefDto(
                patient.getId(),
                patient.getPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone()
        ) : null;

        MedicalRecordResponseDto.DoctorRefDto doctorRef = (doctor != null && doctor.getUser() != null) ? new MedicalRecordResponseDto.DoctorRefDto(
                doctor.getId(),
                doctor.getUser().getFirstName(),
                doctor.getUser().getLastName(),
                doctor.getSpecialization()
        ) : null;

        MedicalRecordResponseDto.AppointmentRefDto appointmentRef = appointment != null ? new MedicalRecordResponseDto.AppointmentRefDto(
                appointment.getId(),
                appointment.getAppointmentNumber(),
                appointment.getAppointmentDate()
        ) : null;

        return new MedicalRecordResponseDto(
                record.getId(),
                patientRef,
                doctorRef,
                appointmentRef,
                record.getSymptoms(),
                record.getDiagnosis(),
                record.getTreatmentPlan(),
                record.getNotes(),
                record.getFollowUpDate(),
                record.isActive(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }

    public MedicalRecordSummaryDto toSummaryDto(MedicalRecord record) {
        if (record == null) {
            return null;
        }

        String patientName = record.getPatient() != null
                ? record.getPatient().getFirstName() + " " + record.getPatient().getLastName()
                : "Unknown Patient";

        String doctorName = (record.getDoctor() != null && record.getDoctor().getUser() != null)
                ? record.getDoctor().getUser().getFirstName() + " " + record.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        String appointmentNumber = record.getAppointment() != null
                ? record.getAppointment().getAppointmentNumber()
                : "N/A";

        return new MedicalRecordSummaryDto(
                record.getId(),
                patientName,
                doctorName,
                appointmentNumber,
                record.getDiagnosis(),
                record.getFollowUpDate(),
                record.isActive(),
                record.getCreatedAt()
        );
    }

    public MedicalRecord toEntity(CreateMedicalRecordRequestDto dto, Patient patient, Doctor doctor, Appointment appointment) {
        return MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .symptoms(dto.symptoms())
                .diagnosis(dto.diagnosis())
                .treatmentPlan(dto.treatmentPlan())
                .notes(dto.notes())
                .followUpDate(dto.followUpDate())
                .isActive(true)
                .build();
    }

    public void updateEntity(MedicalRecord record, UpdateMedicalRecordRequestDto dto) {
        if (dto.symptoms() != null) {
            record.setSymptoms(dto.symptoms());
        }
        if (dto.diagnosis() != null) {
            record.setDiagnosis(dto.diagnosis());
        }
        if (dto.treatmentPlan() != null) {
            record.setTreatmentPlan(dto.treatmentPlan());
        }
        if (dto.notes() != null) {
            record.setNotes(dto.notes());
        }
        if (dto.followUpDate() != null) {
            record.setFollowUpDate(dto.followUpDate());
        }
    }
}
