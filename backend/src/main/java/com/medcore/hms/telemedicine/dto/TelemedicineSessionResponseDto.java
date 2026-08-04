package com.medcore.hms.telemedicine.dto;

import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for a Telemedicine video consultation session")
public record TelemedicineSessionResponseDto(
        @Schema(description = "Session UUID")
        UUID id,

        @Schema(description = "Room code")
        String roomCode,

        @Schema(description = "Video meeting URL")
        String meetingUrl,

        @Schema(description = "Appointment UUID")
        UUID appointmentId,

        @Schema(description = "Doctor UUID")
        UUID doctorId,

        @Schema(description = "Doctor name")
        String doctorName,

        @Schema(description = "Patient UUID")
        UUID patientId,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Scheduled start time")
        LocalDateTime scheduledStartTime,

        @Schema(description = "Actual start time")
        LocalDateTime actualStartTime,

        @Schema(description = "End time")
        LocalDateTime endTime,

        @Schema(description = "Consultation session status")
        ConsultationSessionStatus status,

        @Schema(description = "Summary notes")
        String summaryNotes,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
