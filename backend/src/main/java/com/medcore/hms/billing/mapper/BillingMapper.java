package com.medcore.hms.billing.mapper;

import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.Invoice;
import com.medcore.hms.billing.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillingMapper {

    public InvoiceResponseDto toInvoiceResponseDto(Invoice invoice) {
        if (invoice == null) return null;

        String patientName = invoice.getPatient() != null
                ? invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName()
                : "Unknown Patient";

        List<InvoiceResponseDto.InvoiceItemResponseDto> itemDtos = invoice.getItems() != null
                ? invoice.getItems().stream().map(i -> new InvoiceResponseDto.InvoiceItemResponseDto(
                        i.getId(),
                        i.getDescription(),
                        i.getCategory() != null ? i.getCategory().name() : "OTHER",
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getTotalPrice()
                )).toList()
                : List.of();

        List<PaymentResponseDto> paymentDtos = invoice.getPayments() != null
                ? invoice.getPayments().stream().map(this::toPaymentResponseDto).toList()
                : List.of();

        java.math.BigDecimal discountPct = invoice.getDiscountPercentage();
        if ((discountPct == null || discountPct.compareTo(java.math.BigDecimal.ZERO) == 0)
                && invoice.getDiscountAmount() != null
                && invoice.getSubtotal() != null
                && invoice.getSubtotal().compareTo(java.math.BigDecimal.ZERO) > 0) {
            discountPct = invoice.getDiscountAmount()
                    .multiply(java.math.BigDecimal.valueOf(100))
                    .divide(invoice.getSubtotal(), 2, java.math.RoundingMode.HALF_UP);
        }
        if (discountPct == null) discountPct = java.math.BigDecimal.ZERO;

        return new InvoiceResponseDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getPatient() != null ? invoice.getPatient().getId() : null,
                patientName,
                invoice.getAppointment() != null ? invoice.getAppointment().getId() : null,
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getSubtotal(),
                invoice.getTaxAmount(),
                invoice.getDiscountAmount(),
                discountPct,
                invoice.getGrandTotal(),
                invoice.getPaidAmount(),
                invoice.getBalanceDue(),
                invoice.getStatus(),
                itemDtos,
                paymentDtos,
                invoice.getCreatedAt()
        );
    }

    public InvoiceSummaryDto toInvoiceSummaryDto(Invoice invoice) {
        if (invoice == null) return null;

        String patientName = invoice.getPatient() != null
                ? invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName()
                : "Unknown Patient";

        return new InvoiceSummaryDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                patientName,
                invoice.getIssueDate(),
                invoice.getGrandTotal(),
                invoice.getBalanceDue(),
                invoice.getStatus()
        );
    }

    public PaymentResponseDto toPaymentResponseDto(Payment p) {
        if (p == null) return null;

        String processedByName = p.getProcessedBy() != null
                ? p.getProcessedBy().getFirstName() + " " + p.getProcessedBy().getLastName()
                : null;

        return new PaymentResponseDto(
                p.getId(),
                p.getReceiptNumber(),
                p.getInvoice() != null ? p.getInvoice().getId() : null,
                p.getAmount(),
                p.getPaymentMethod(),
                p.getTransactionReference(),
                p.getPaidAt(),
                p.getStatus(),
                processedByName,
                p.getRemarks()
        );
    }
}
