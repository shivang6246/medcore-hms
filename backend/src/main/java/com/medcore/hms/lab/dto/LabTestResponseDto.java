package com.medcore.hms.lab.dto;

import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.entity.TestPriority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for a Lab Test order")
public record LabTestResponseDto(
        @Schema(description = "Lab test UUID")
        UUID id,

        @Schema(description = "Patient reference")
        PatientRefDto patient,

        @Schema(description = "Doctor reference")
        DoctorRefDto doctor,

        @Schema(description = "Appointment UUID")
        UUID appointmentId,

        @Schema(description = "Medical record UUID")
        UUID medicalRecordId,

        @Schema(description = "Technician name if assigned")
        String technicianName,

        @Schema(description = "Test type ordered")
        String testType,

        @Schema(description = "Test priority")
        TestPriority priority,

        @Schema(description = "Current status")
        LabTestStatus status,

        @Schema(description = "Special instructions")
        String instructions,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Associated report if completed")
        LabReportResponseDto labReport,

        @Schema(description = "Order creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {
    public record PatientRefDto(
            UUID id,
            String patientId,
            String firstName,
            String lastName,
            String phone
    ) {}

    public record DoctorRefDto(
            UUID id,
            String firstName,
            String lastName,
            String specialization
    ) {}
}
