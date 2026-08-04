package com.medcore.hms.pharmacy.dto;

import com.medcore.hms.pharmacy.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for a Stock Transaction audit record")
public record StockTransactionResponseDto(
        @Schema(description = "Transaction UUID")
        UUID id,

        @Schema(description = "Medicine name")
        String medicineName,

        @Schema(description = "Batch number if applicable")
        String batchNumber,

        @Schema(description = "Transaction type")
        TransactionType transactionType,

        @Schema(description = "Quantity delta")
        Integer quantity,

        @Schema(description = "Stock level after transaction")
        Integer quantityAfter,

        @Schema(description = "Transaction date")
        LocalDateTime transactionDate,

        @Schema(description = "Name of user who performed action")
        String performedByName,

        @Schema(description = "Reason or remarks")
        String reason
) {}
