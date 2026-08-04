package com.medcore.hms.billing.controller;

import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.InvoiceStatus;
import com.medcore.hms.billing.service.BillingService;
import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Billing & Payments Management", description = "REST APIs for invoice generation, payment collection, refunds, and financial revenue reports.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    // ── Invoice APIs ─────────────────────────────────────────────────────────

    @Operation(summary = "Generate a new invoice", description = "Creates an itemized invoice for consultation, lab, or pharmacy charges.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Invoice created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    @PostMapping("/api/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequestDto dto) {
        log.info("REST request to generate invoice for patient ID: {}", dto.patientId());
        InvoiceResponseDto response = billingService.createInvoice(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invoice generated successfully"));
    }

    @Operation(summary = "Get invoice by ID")
    @GetMapping("/api/invoices/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT', 'PATIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponseDto>> getInvoiceById(
            @Parameter(description = "Invoice UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch invoice ID: {}", id);
        InvoiceResponseDto response = billingService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Invoice fetched successfully"));
    }

    @Operation(summary = "List invoices for a Patient (paginated)")
    @GetMapping("/api/patients/{id}/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceSummaryDto>>> getInvoicesByPatient(
            @Parameter(description = "Patient UUID", required = true) @PathVariable("id") UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate,desc") String sort) {
        log.info("REST request to fetch invoices for patient ID: {}", patientId);
        PagedResponse<InvoiceSummaryDto> response = billingService.getInvoicesByPatient(
                patientId, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Patient invoices fetched successfully"));
    }

    @Operation(summary = "List all invoices (paginated, optional status filter)")
    @GetMapping("/api/invoices")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceSummaryDto>>> getAllInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "issueDate,desc") String sort) {
        log.info("REST request to fetch all invoices paginated (status filter: {})", status);
        PagedResponse<InvoiceSummaryDto> response = billingService.getAllInvoices(
                status, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Invoices fetched successfully"));
    }

    // ── Payment APIs ─────────────────────────────────────────────────────────

    @Operation(summary = "Process payment for an invoice", description = "Records a full or partial payment against an open invoice.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payment recorded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Overpayment or validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Invoice not found")
    })
    @PostMapping("/api/payments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(
            @Valid @RequestBody ProcessPaymentRequestDto dto) {
        log.info("REST request to process payment for invoice ID: {}", dto.invoiceId());
        PaymentResponseDto response = billingService.processPayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment processed successfully"));
    }

    @Operation(summary = "Get payment transaction by ID")
    @GetMapping("/api/payments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'ACCOUNTANT', 'PATIENT')")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> getPaymentById(
            @Parameter(description = "Payment UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch payment ID: {}", id);
        PaymentResponseDto response = billingService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment details fetched successfully"));
    }

    // ── Refund & Reports ─────────────────────────────────────────────────────

    @Operation(summary = "Process refund for an invoice")
    @PostMapping("/api/invoices/{id}/refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<RefundResponseDto>> refundInvoice(
            @Parameter(description = "Invoice UUID", required = true) @PathVariable("id") UUID invoiceId,
            @Valid @RequestBody RefundRequestDto dto) {
        log.info("REST request to process refund for invoice ID: {}", invoiceId);
        RefundResponseDto response = billingService.refundInvoice(invoiceId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund issued successfully"));
    }

    @Operation(summary = "Generate revenue and financial metrics report")
    @GetMapping("/api/billing/reports/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT')")
    public ResponseEntity<ApiResponse<RevenueReportDto>> getRevenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("REST request to fetch revenue report from {} to {}", startDate, endDate);
        RevenueReportDto response = billingService.getRevenueReport(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Revenue report generated successfully"));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "issueDate");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, property);
    }
}
