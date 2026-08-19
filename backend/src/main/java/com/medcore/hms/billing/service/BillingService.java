package com.medcore.hms.billing.service;

import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.InvoiceStatus;
import com.medcore.hms.common.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface BillingService {

    InvoiceResponseDto createInvoice(CreateInvoiceRequestDto dto);

    InvoiceResponseDto getInvoiceById(UUID id);

    PagedResponse<InvoiceSummaryDto> getInvoicesByPatient(UUID patientId, Pageable pageable);

    PagedResponse<InvoiceSummaryDto> getAllInvoices(InvoiceStatus status, Pageable pageable);

    PaymentResponseDto processPayment(ProcessPaymentRequestDto dto);

    PaymentResponseDto getPaymentById(UUID id);

    RefundResponseDto refundInvoice(UUID invoiceId, RefundRequestDto dto);

    RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate);

    PaymentResponseDto processStripePayment(UUID invoiceId, StripePaymentRequestDto dto);

    byte[] generateInvoicePdf(UUID invoiceId);
}
