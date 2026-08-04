package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.entity.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Summary DTO for Invoice listings")
public record InvoiceSummaryDto(
        @Schema(description = "Invoice UUID")
        UUID id,

        @Schema(description = "Invoice number")
        String invoiceNumber,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Issue date")
        LocalDate issueDate,

        @Schema(description = "Grand total")
        BigDecimal grandTotal,

        @Schema(description = "Balance due")
        BigDecimal balanceDue,

        @Schema(description = "Status")
        InvoiceStatus status
) {}
