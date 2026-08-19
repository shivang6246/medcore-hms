package com.medcore.hms.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

@Schema(description = "Request payload for Stripe online payment")
public record StripePaymentRequestDto(
        @Schema(description = "Stripe payment token or method ID", example = "tok_visa")
        String stripeToken,

        @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
        @Schema(description = "Optional custom amount to pay (defaults to remaining balance due)", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Currency code (ISO 4217)", example = "INR")
        String currency,

        @Schema(description = "Payment method type (CARD, UPI)", example = "UPI")
        String paymentMethodType,

        @Schema(description = "Patient Virtual Payment Address / UPI ID", example = "patient@okaxis")
        String upiId,

        @Schema(description = "Payment remarks or note", example = "Stripe online payment")
        String remarks
) {}
