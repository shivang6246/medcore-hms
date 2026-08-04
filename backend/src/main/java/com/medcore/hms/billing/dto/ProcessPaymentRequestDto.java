package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request payload to process a payment against an invoice")
public record ProcessPaymentRequestDto(
        @NotNull(message = "Invoice ID is required")
        @Schema(description = "Invoice UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID invoiceId,

        @NotNull(message = "Payment amount is required")
        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        @Schema(description = "Amount to pay", example = "100.00")
        BigDecimal amount,

        @NotNull(message = "Payment method is required")
        @Schema(description = "Payment method", example = "UPI")
        PaymentMethod paymentMethod,

        @Schema(description = "External transaction reference / UTR / Auth Code", example = "UPI-REF-998822")
        String transactionReference,

        @Schema(description = "User ID processing payment")
        UUID processedById,

        @Schema(description = "Optional payment remarks", example = "Paid via GPay")
        String remarks
) {}
