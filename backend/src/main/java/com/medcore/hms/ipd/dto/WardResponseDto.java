package com.medcore.hms.ipd.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response DTO for IPD Ward")
public record WardResponseDto(
        @Schema(description = "Ward UUID")
        UUID id,

        @Schema(description = "Ward name")
        String name,

        @Schema(description = "Ward category")
        String category,

        @Schema(description = "Total capacity")
        Integer capacity,

        @Schema(description = "Description")
        String description,

        @Schema(description = "Active status")
        boolean isActive
) {}
