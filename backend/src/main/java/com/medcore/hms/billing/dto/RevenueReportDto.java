package com.medcore.hms.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Revenue and financial metrics report DTO")
public record RevenueReportDto(
        @Schema(description = "Report start date")
        LocalDate startDate,

        @Schema(description = "Report end date")
        LocalDate endDate,

        @Schema(description = "Total invoiced amount")
        BigDecimal totalInvoicedAmount,

        @Schema(description = "Total payments collected")
        BigDecimal totalCollectedAmount,

        @Schema(description = "Total outstanding balance due")
        BigDecimal totalOutstandingAmount,

        @Schema(description = "Total refunded amount")
        BigDecimal totalRefundedAmount,

        @Schema(description = "Total invoice count")
        long totalInvoiceCount,

        @Schema(description = "Total payments transaction count")
        long totalPaymentCount
) {}
