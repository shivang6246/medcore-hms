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
