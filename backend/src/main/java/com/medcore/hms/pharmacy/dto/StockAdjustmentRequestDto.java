package com.medcore.hms.pharmacy.dto;

import com.medcore.hms.pharmacy.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for manual stock adjustment or audit record")
public record StockAdjustmentRequestDto(
        @NotNull(message = "Medicine ID is required")
        @Schema(description = "Medicine UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID medicineId,

        @Schema(description = "Batch UUID if adjustment applies to specific batch")
        UUID batchId,

        @NotNull(message = "Transaction type is required")
        @Schema(description = "Transaction type", example = "ADJUSTMENT")
        TransactionType transactionType,

        @NotNull(message = "Quantity change is required")
        @Schema(description = "Quantity change (positive to increase, negative to reduce)", example = "-5")
        Integer quantityChange,

        @Schema(description = "Reason for adjustment", example = "Damaged packaging / Stock audit adjustment")
        String reason,

        @Schema(description = "User ID performing adjustment")
        UUID performedById
) {}
