package com.medcore.hms.telemedicine.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response payload containing video room credentials and access tokens to join virtual consultation")
public record JoinSessionResponseDto(
        @Schema(description = "Session UUID")
        UUID sessionId,

        @Schema(description = "Room code")
        String roomCode,

        @Schema(description = "WebRTC / Video room URL")
        String meetingUrl,

        @Schema(description = "Participant role", example = "DOCTOR")
        String role,

        @Schema(description = "Secure room token")
        String token
) {}
