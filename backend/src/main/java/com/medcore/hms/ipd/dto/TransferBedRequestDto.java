package com.medcore.hms.ipd.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload to transfer patient to a new ward/room/bed")
public record TransferBedRequestDto(
        @NotNull(message = "New Ward ID is required")
        @Schema(description = "Target Ward UUID")
        UUID newWardId,

        @NotNull(message = "New Room ID is required")
        @Schema(description = "Target Room UUID")
        UUID newRoomId,

        @NotNull(message = "New Bed ID is required")
        @Schema(description = "Target Bed UUID")
        UUID newBedId,

        @Schema(description = "Reason for transfer", example = "Transferred to ICU post-op")
        String reason
) {}
