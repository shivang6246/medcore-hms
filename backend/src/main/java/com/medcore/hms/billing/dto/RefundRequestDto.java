package com.medcore.hms.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request payload to process an invoice refund")
public record RefundRequestDto(
        @NotNull(message = "Refund amount is required")
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than zero")
        @Schema(description = "Amount to refund", example = "50.00")
        BigDecimal refundAmount,

        @NotBlank(message = "Refund reason is required")
        @Schema(description = "Reason for refund", example = "Patient cancelled lab test before processing")
        String reason,

        @Schema(description = "User ID issuing refund")
        UUID processedById
) {}
