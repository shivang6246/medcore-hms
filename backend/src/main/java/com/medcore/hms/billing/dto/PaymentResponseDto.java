package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.entity.PaymentMethod;
import com.medcore.hms.billing.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for Payment transaction")
public record PaymentResponseDto(
        @Schema(description = "Payment UUID")
        UUID id,

        @Schema(description = "Receipt reference number")
        String receiptNumber,

        @Schema(description = "Associated invoice UUID")
        UUID invoiceId,

        @Schema(description = "Payment amount")
        BigDecimal amount,

        @Schema(description = "Payment method")
        PaymentMethod paymentMethod,

        @Schema(description = "Transaction reference")
        String transactionReference,

        @Schema(description = "Paid timestamp")
        LocalDateTime paidAt,

        @Schema(description = "Payment status")
        PaymentStatus status,

        @Schema(description = "User who processed payment")
        String processedByName,

        @Schema(description = "Remarks")
        String remarks
) {}
