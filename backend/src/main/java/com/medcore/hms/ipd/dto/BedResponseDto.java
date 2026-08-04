package com.medcore.hms.ipd.dto;

import com.medcore.hms.ipd.entity.BedStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Response DTO for IPD Bed")
public record BedResponseDto(
        @Schema(description = "Bed UUID")
        UUID id,

        @Schema(description = "Room UUID")
        UUID roomId,

        @Schema(description = "Room number")
        String roomNumber,

        @Schema(description = "Ward name")
        String wardName,

        @Schema(description = "Bed number")
        String bedNumber,

        @Schema(description = "Bed status")
        BedStatus status,

        @Schema(description = "Daily rate")
        BigDecimal dailyRate,

        @Schema(description = "Active status")
        boolean isActive
) {}
