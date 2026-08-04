package com.medcore.hms.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for Refund operation")
public record RefundResponseDto(
        @Schema(description = "Invoice UUID")
        UUID invoiceId,

        @Schema(description = "Invoice reference number")
        String invoiceNumber,

        @Schema(description = "Refunded amount")
        BigDecimal refundedAmount,

        @Schema(description = "Updated invoice status")
        String status,

        @Schema(description = "Reason for refund")
        String reason,

        @Schema(description = "Refund timestamp")
        LocalDateTime refundedAt
) {}
