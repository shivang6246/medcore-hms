package com.medcore.hms.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload to create a new patient invoice")
public record CreateInvoiceRequestDto(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "Patient UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID patientId,

        @Schema(description = "Optional appointment UUID", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID appointmentId,

        @NotNull(message = "Issue date is required")
        @Schema(description = "Invoice issue date", example = "2026-08-04")
        LocalDate issueDate,

        @Schema(description = "Invoice payment due date", example = "2026-08-18")
        LocalDate dueDate,

        @DecimalMin(value = "0.00", message = "Tax amount cannot be negative")
        @Schema(description = "Tax amount", example = "15.00")
        BigDecimal taxAmount,

        @DecimalMin(value = "0.00", message = "Discount amount cannot be negative")
        @Schema(description = "Discount amount", example = "10.00")
        BigDecimal discountAmount,

        @NotEmpty(message = "Invoice line items list cannot be empty")
        @Valid
        @Schema(description = "Line items for consultation, lab tests, pharmacy, etc.")
        List<InvoiceItemRequestDto> items
) {}
