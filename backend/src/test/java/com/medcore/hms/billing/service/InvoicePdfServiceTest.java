package com.medcore.hms.billing.service;

import com.medcore.hms.billing.entity.Invoice;
import com.medcore.hms.billing.entity.InvoiceItem;
import com.medcore.hms.billing.entity.InvoiceStatus;
import com.medcore.hms.billing.entity.ItemCategory;
import com.medcore.hms.patient.entity.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InvoicePdfService Unit Tests")
class InvoicePdfServiceTest {

    private final InvoicePdfService pdfService = new InvoicePdfService();

    @Test
    @DisplayName("Should generate non-empty PDF byte array for valid invoice")
    void generateInvoicePdf_Success() {
        Patient patient = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .patientId("PAT-1001")
                .email("jane.smith@example.com")
                .phone("+1999888777")
                .build();

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-9999")
                .patient(patient)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .subtotal(new BigDecimal("150.00"))
                .taxAmount(new BigDecimal("15.00"))
                .discountAmount(new BigDecimal("10.00"))
                .grandTotal(new BigDecimal("155.00"))
                .paidAmount(new BigDecimal("155.00"))
                .balanceDue(BigDecimal.ZERO)
                .status(InvoiceStatus.PAID)
                .build();

        InvoiceItem item1 = InvoiceItem.builder()
                .invoice(invoice)
                .description("Doctor Consultation")
                .category(ItemCategory.CONSULTATION)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(1)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        InvoiceItem item2 = InvoiceItem.builder()
                .invoice(invoice)
                .description("Blood Test")
                .category(ItemCategory.LAB_TEST)
                .unitPrice(new BigDecimal("50.00"))
                .quantity(1)
                .totalPrice(new BigDecimal("50.00"))
                .build();

        invoice.setItems(List.of(item1, item2));

        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // PDF header check
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertThat(pdfHeader).isEqualTo("%PDF-");
    }
}
