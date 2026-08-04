package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for a MedicineBatch entity")
public record MedicineBatchResponseDto(
        @Schema(description = "Batch UUID")
        UUID id,

        @Schema(description = "Associated Medicine UUID")
        UUID medicineId,

        @Schema(description = "Medicine name")
        String medicineName,

        @Schema(description = "Supplier name")
        String supplierName,

        @Schema(description = "Batch number")
        String batchNumber,

        @Schema(description = "Expiration date")
        LocalDate expiryDate,

        @Schema(description = "Purchase price per unit")
        BigDecimal purchasePrice,

        @Schema(description = "Selling price per unit")
        BigDecimal sellingPrice,

        @Schema(description = "Initial received quantity")
        Integer initialQuantity,

        @Schema(description = "Current available quantity")
        Integer currentQuantity,

        @Schema(description = "Expired indicator")
        boolean isExpired,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
