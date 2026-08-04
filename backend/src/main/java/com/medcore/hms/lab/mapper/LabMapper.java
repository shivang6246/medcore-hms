package com.medcore.hms.lab.mapper;

import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.lab.dto.*;
import com.medcore.hms.lab.entity.LabReport;
import com.medcore.hms.lab.entity.LabTest;
import com.medcore.hms.patient.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class LabMapper {

    public LabTestResponseDto toLabTestResponseDto(LabTest test) {
        if (test == null) return null;

        Patient patient = test.getPatient();
        Doctor doctor = test.getDoctor();

        LabTestResponseDto.PatientRefDto patientRef = patient != null ? new LabTestResponseDto.PatientRefDto(
                patient.getId(),
                patient.getPatientId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhone()
        ) : null;

        LabTestResponseDto.DoctorRefDto doctorRef = (doctor != null && doctor.getUser() != null) ? new LabTestResponseDto.DoctorRefDto(
                doctor.getId(),
                doctor.getUser().getFirstName(),
                doctor.getUser().getLastName(),
                doctor.getSpecialization()
        ) : null;

        String techName = test.getTechnician() != null
                ? test.getTechnician().getFirstName() + " " + test.getTechnician().getLastName()
                : null;

        LabReportResponseDto reportDto = toLabReportResponseDto(test.getLabReport());

        return new LabTestResponseDto(
                test.getId(),
                patientRef,
                doctorRef,
                test.getAppointment() != null ? test.getAppointment().getId() : null,
                test.getMedicalRecord() != null ? test.getMedicalRecord().getId() : null,
                techName,
                test.getTestType(),
                test.getPriority(),
                test.getStatus(),
                test.getInstructions(),
                test.isActive(),
                reportDto,
                test.getCreatedAt(),
                test.getUpdatedAt()
        );
    }

    public LabTestSummaryDto toLabTestSummaryDto(LabTest test) {
        if (test == null) return null;

        String patientName = test.getPatient() != null
                ? test.getPatient().getFirstName() + " " + test.getPatient().getLastName()
                : "Unknown Patient";

        String doctorName = (test.getDoctor() != null && test.getDoctor().getUser() != null)
                ? test.getDoctor().getUser().getFirstName() + " " + test.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        return new LabTestSummaryDto(
                test.getId(),
                patientName,
                doctorName,
                test.getTestType(),
                test.getPriority(),
                test.getStatus(),
                test.getCreatedAt()
        );
    }

    public LabReportResponseDto toLabReportResponseDto(LabReport report) {
        if (report == null) return null;

        String reportedByName = report.getReportedBy() != null
                ? report.getReportedBy().getFirstName() + " " + report.getReportedBy().getLastName()
                : null;

        return new LabReportResponseDto(
                report.getId(),
                report.getLabTest() != null ? report.getLabTest().getId() : null,
                report.getResult(),
                report.getRemarks(),
                report.getReportFileUrl(),
                report.getReportedAt(),
                reportedByName
        );
    }
}
