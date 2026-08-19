package com.medcore.hms.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.InvoiceStatus;
import com.medcore.hms.billing.entity.ItemCategory;
import com.medcore.hms.billing.entity.PaymentMethod;
import com.medcore.hms.billing.entity.PaymentStatus;
import com.medcore.hms.billing.exception.InvoiceNotFoundException;
import com.medcore.hms.billing.service.BillingService;
import com.medcore.hms.common.dto.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("BillingController Integration Tests")
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BillingService billingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID invoiceId;
    private UUID patientId;

    private CreateInvoiceRequestDto createDto;
    private InvoiceResponseDto responseDto;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        createDto = new CreateInvoiceRequestDto(
                patientId, null, LocalDate.now(), LocalDate.now().plusDays(14),
                new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                List.of(new InvoiceItemRequestDto("Consultation", ItemCategory.CONSULTATION, new BigDecimal("100.00"), 1))
        );

        responseDto = new InvoiceResponseDto(
                invoiceId, "INV-2026-01", patientId, "John Doe", null, LocalDate.now(), LocalDate.now().plusDays(14),
                new BigDecimal("100.00"), new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("110.00"), BigDecimal.ZERO, new BigDecimal("110.00"),
                InvoiceStatus.UNPAID, List.of(), List.of(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/invoices")
    class CreateInvoiceApi {

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("201 Created — Accountant generates invoice")
        void createInvoice_Success() throws Exception {
            when(billingService.createInvoice(any(CreateInvoiceRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(invoiceId.toString()))
                    .andExpect(jsonPath("$.data.invoiceNumber").value("INV-2026-01"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot create invoice")
        void createInvoice_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/invoices/{id}")
    class GetInvoiceApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Fetch invoice by ID")
        void getInvoiceById_Success() throws Exception {
            when(billingService.getInvoiceById(invoiceId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/invoices/{id}", invoiceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(invoiceId.toString()));
        }

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("404 Not Found — Invoice missing")
        void getInvoiceById_NotFound() throws Exception {
            when(billingService.getInvoiceById(invoiceId))
                    .thenThrow(new InvoiceNotFoundException(invoiceId));

            mockMvc.perform(get("/api/invoices/{id}", invoiceId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/payments & POST /api/invoices/{id}/refund")
    class PaymentAndRefundApi {

        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("201 Created — Process payment")
        void processPayment_Success() throws Exception {
            ProcessPaymentRequestDto paymentDto = new ProcessPaymentRequestDto(
                    invoiceId, new BigDecimal("110.00"), PaymentMethod.CARD, "CARD-REF-100", null, "Paid at desk"
            );

            PaymentResponseDto paymentResponse = new PaymentResponseDto(
                    UUID.randomUUID(), "RCP-100", invoiceId, new BigDecimal("110.00"),
                    PaymentMethod.CARD, "CARD-REF-100", LocalDateTime.now(), PaymentStatus.SUCCESS, "Receptionist", "Paid at desk"
            );

            when(billingService.processPayment(any(ProcessPaymentRequestDto.class))).thenReturn(paymentResponse);

            mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.receiptNumber").value("RCP-100"));
        }

        @Test
        @WithMockUser(roles = "ACCOUNTANT")
        @DisplayName("200 OK — Refund invoice")
        void refundInvoice_Success() throws Exception {
            RefundRequestDto refundDto = new RefundRequestDto(new BigDecimal("110.00"), "Service cancelled", null);
            RefundResponseDto refundResponse = new RefundResponseDto(
                    invoiceId, "INV-2026-01", new BigDecimal("110.00"), "REFUNDED", "Service cancelled", LocalDateTime.now()
            );

            when(billingService.refundInvoice(eq(invoiceId), any(RefundRequestDto.class))).thenReturn(refundResponse);

            mockMvc.perform(post("/api/invoices/{id}/refund", invoiceId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.refundedAmount").value(110.00));
        }
    }

    @Nested
    @DisplayName("GET /api/billing/reports/revenue")
    class RevenueReportApi {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 OK — Generate revenue report")
        void getRevenueReport_Success() throws Exception {
            RevenueReportDto reportDto = new RevenueReportDto(
                    LocalDate.now().minusDays(30), LocalDate.now(),
                    new BigDecimal("10000.00"), new BigDecimal("8500.00"),
                    new BigDecimal("1500.00"), new BigDecimal("200.00"), 50, 45
            );

            when(billingService.getRevenueReport(any(), any())).thenReturn(reportDto);

            mockMvc.perform(get("/api/billing/reports/revenue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalInvoicedAmount").value(10000.00));
        }
    }
}
