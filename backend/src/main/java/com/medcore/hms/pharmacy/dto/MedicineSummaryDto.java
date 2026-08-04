package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Summary DTO for Medicine listings")
public record MedicineSummaryDto(
        @Schema(description = "Medicine UUID")
        UUID id,

        @Schema(description = "Commercial name")
        String name,

        @Schema(description = "Category")
        String category,

        @Schema(description = "Strength")
        String strength,

        @Schema(description = "Unit price")
        BigDecimal unitPrice,

        @Schema(description = "Stock quantity")
        Integer stockQuantity,

        @Schema(description = "Low stock alert indicator")
        boolean isLowStock,

        @Schema(description = "Active status")
        boolean isActive
) {}
