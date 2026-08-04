package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.entity.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Detailed response DTO for an Invoice")
public record InvoiceResponseDto(
        @Schema(description = "Invoice UUID")
        UUID id,

        @Schema(description = "Invoice reference number")
        String invoiceNumber,

        @Schema(description = "Patient UUID")
        UUID patientId,

        @Schema(description = "Patient full name")
        String patientName,

        @Schema(description = "Appointment UUID")
        UUID appointmentId,

        @Schema(description = "Issue date")
        LocalDate issueDate,

        @Schema(description = "Due date")
        LocalDate dueDate,

        @Schema(description = "Subtotal amount")
        BigDecimal subtotal,

        @Schema(description = "Tax amount")
        BigDecimal taxAmount,

        @Schema(description = "Discount amount")
        BigDecimal discountAmount,

        @Schema(description = "Grand total amount")
        BigDecimal grandTotal,

        @Schema(description = "Amount paid so far")
        BigDecimal paidAmount,

        @Schema(description = "Remaining balance due")
        BigDecimal balanceDue,

        @Schema(description = "Invoice status")
        InvoiceStatus status,

        @Schema(description = "Line items")
        List<InvoiceItemResponseDto> items,

        @Schema(description = "Payment history transactions")
        List<PaymentResponseDto> payments,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {
    public record InvoiceItemResponseDto(
            UUID id,
            String description,
            String category,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal totalPrice
    ) {}
}
