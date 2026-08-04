package com.medcore.hms.telemedicine.dto;

import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Summary DTO for virtual waiting room queue and consultation history")
public record TelemedicineSessionSummaryDto(
        @Schema(description = "Session UUID")
        UUID id,

        @Schema(description = "Room code")
        String roomCode,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Doctor name")
        String doctorName,

        @Schema(description = "Scheduled start time")
        LocalDateTime scheduledStartTime,

        @Schema(description = "Status")
        ConsultationSessionStatus status
) {}
