package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Detailed response DTO for a Medicine entity")
public record MedicineResponseDto(
        @Schema(description = "Medicine UUID")
        UUID id,

        @Schema(description = "Medicine commercial name")
        String name,

        @Schema(description = "Generic chemical name")
        String genericName,

        @Schema(description = "Brand name")
        String brand,

        @Schema(description = "Category")
        String category,

        @Schema(description = "Strength")
        String strength,

        @Schema(description = "Selling price per unit")
        BigDecimal unitPrice,

        @Schema(description = "Manufacturer")
        String manufacturer,

        @Schema(description = "Barcode")
        String barcode,

        @Schema(description = "Total stock quantity across all batches")
        Integer stockQuantity,

        @Schema(description = "Reorder level threshold")
        Integer reorderLevel,

        @Schema(description = "Low stock indicator")
        boolean isLowStock,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt
) {}
