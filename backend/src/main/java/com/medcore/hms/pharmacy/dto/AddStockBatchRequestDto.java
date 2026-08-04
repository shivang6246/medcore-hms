package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Request payload to add new inventory stock (batch)")
public record AddStockBatchRequestDto(
        @NotNull(message = "Medicine ID is required")
        @Schema(description = "Medicine UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID medicineId,

        @Schema(description = "Optional supplier UUID", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID supplierId,

        @NotBlank(message = "Batch number is required")
        @Schema(description = "Unique batch code/number", example = "BATCH-2026-08A")
        String batchNumber,

        @NotNull(message = "Expiry date is required")
        @Future(message = "Expiry date must be in the future")
        @Schema(description = "Batch expiration date", example = "2027-08-31")
        LocalDate expiryDate,

        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Schema(description = "Purchase price per unit", example = "3.20")
        BigDecimal purchasePrice,

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Schema(description = "Selling price per unit", example = "5.50")
        BigDecimal sellingPrice,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Stock quantity received", example = "500")
        Integer quantity,

        @Schema(description = "User ID performing the stock update")
        UUID performedById
) {}
