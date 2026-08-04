package com.medcore.hms.billing.service;

import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.*;
import com.medcore.hms.billing.exception.InvalidRefundException;
import com.medcore.hms.billing.exception.InvoiceNotFoundException;
import com.medcore.hms.billing.exception.OverpaymentException;
import com.medcore.hms.billing.mapper.BillingMapper;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.billing.repository.PaymentRepository;
import com.medcore.hms.billing.service.impl.BillingServiceImpl;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingService Unit Tests")
class BillingServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private BillingMapper billingMapper;

    @InjectMocks
    private BillingServiceImpl billingService;

    private UUID invoiceId;
    private UUID patientId;
    private Patient patient;
    private Invoice invoice;
    private CreateInvoiceRequestDto createInvoiceDto;
    private InvoiceResponseDto invoiceResponseDto;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        patient = Patient.builder().firstName("John").lastName("Doe").build();
        patient.setId(patientId);

        invoice = Invoice.builder()
                .invoiceNumber("INV-100")
                .patient(patient)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .subtotal(new BigDecimal("200.00"))
                .taxAmount(new BigDecimal("20.00"))
                .discountAmount(new BigDecimal("10.00"))
                .grandTotal(new BigDecimal("210.00"))
                .paidAmount(BigDecimal.ZERO)
                .balanceDue(new BigDecimal("210.00"))
                .status(InvoiceStatus.UNPAID)
                .build();
        invoice.setId(invoiceId);

        createInvoiceDto = new CreateInvoiceRequestDto(
                patientId, null, LocalDate.now(), LocalDate.now().plusDays(14),
                new BigDecimal("20.00"), new BigDecimal("10.00"),
                List.of(new InvoiceItemRequestDto("Consultation", ItemCategory.CONSULTATION, new BigDecimal("200.00"), 1))
        );

        invoiceResponseDto = new InvoiceResponseDto(
                invoiceId, "INV-100", patientId, "John Doe", null, LocalDate.now(), LocalDate.now().plusDays(14),
                new BigDecimal("200.00"), new BigDecimal("20.00"), new BigDecimal("10.00"),
                new BigDecimal("210.00"), BigDecimal.ZERO, new BigDecimal("210.00"),
                InvoiceStatus.UNPAID, List.of(), List.of(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Create Invoice Tests")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create invoice with calculated grand total")
        void createInvoice_Success() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(billingMapper.toInvoiceResponseDto(invoice)).thenReturn(invoiceResponseDto);

            InvoiceResponseDto result = billingService.createInvoice(createInvoiceDto);

            assertThat(result).isNotNull();
            assertThat(result.invoiceNumber()).isEqualTo("INV-100");
            verify(invoiceRepository, times(2)).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient missing")
        void createInvoice_PatientNotFound() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> billingService.createInvoice(createInvoiceDto))
                    .isInstanceOf(PatientNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Process Payment Tests")
    class PaymentTests {

        @Test
        @DisplayName("Should process partial payment successfully")
        void processPayment_Partial_Success() {
            ProcessPaymentRequestDto paymentDto = new ProcessPaymentRequestDto(
                    invoiceId, new BigDecimal("100.00"), PaymentMethod.CASH, "REF-1", null, "Partial pay"
            );

            Payment payment = Payment.builder()
                    .receiptNumber("RCP-1")
                    .invoice(invoice)
                    .amount(new BigDecimal("100.00"))
                    .paymentMethod(PaymentMethod.CASH)
                    .status(PaymentStatus.SUCCESS)
                    .build();

            PaymentResponseDto paymentResponse = new PaymentResponseDto(
                    UUID.randomUUID(), "RCP-1", invoiceId, new BigDecimal("100.00"),
                    PaymentMethod.CASH, "REF-1", LocalDateTime.now(), PaymentStatus.SUCCESS, null, "Notes"
            );

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(billingMapper.toPaymentResponseDto(payment)).thenReturn(paymentResponse);

            PaymentResponseDto result = billingService.processPayment(paymentDto);

            assertThat(result).isNotNull();
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo("100.00");
            assertThat(invoice.getBalanceDue()).isEqualByComparingTo("110.00");
        }

        @Test
        @DisplayName("Should process full payment and update status to PAID")
        void processPayment_Full_Success() {
            ProcessPaymentRequestDto paymentDto = new ProcessPaymentRequestDto(
                    invoiceId, new BigDecimal("210.00"), PaymentMethod.UPI, "UPI-1", null, "Full pay"
            );

            Payment payment = Payment.builder().amount(new BigDecimal("210.00")).build();
            PaymentResponseDto paymentResponse = new PaymentResponseDto(
                    UUID.randomUUID(), "RCP-2", invoiceId, new BigDecimal("210.00"),
                    PaymentMethod.UPI, "UPI-1", LocalDateTime.now(), PaymentStatus.SUCCESS, null, "Full pay"
            );

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
            when(billingMapper.toPaymentResponseDto(payment)).thenReturn(paymentResponse);

            billingService.processPayment(paymentDto);

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(invoice.getBalanceDue()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("Should throw OverpaymentException when amount exceeds balance due")
        void processPayment_OverpaymentError() {
            ProcessPaymentRequestDto paymentDto = new ProcessPaymentRequestDto(
                    invoiceId, new BigDecimal("300.00"), PaymentMethod.CASH, null, null, null
            );

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> billingService.processPayment(paymentDto))
                    .isInstanceOf(OverpaymentException.class)
                    .hasMessageContaining("exceeds balance due");
        }
    }

    @Nested
    @DisplayName("Refund Tests")
    class RefundTests {

        @Test
        @DisplayName("Should issue refund successfully")
        void refundInvoice_Success() {
            invoice.setPaidAmount(new BigDecimal("100.00"));
            invoice.setBalanceDue(new BigDecimal("110.00"));
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);

            RefundRequestDto refundDto = new RefundRequestDto(new BigDecimal("100.00"), "Lab test cancelled", null);

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            RefundResponseDto result = billingService.refundInvoice(invoiceId, refundDto);

            assertThat(result).isNotNull();
            assertThat(invoice.getPaidAmount()).isEqualByComparingTo("0.00");
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should throw InvalidRefundException when refund exceeds paid amount")
        void refundInvoice_ExceedsPaidError() {
            invoice.setPaidAmount(new BigDecimal("50.00"));

            RefundRequestDto refundDto = new RefundRequestDto(new BigDecimal("100.00"), "Excess refund", null);

            when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> billingService.refundInvoice(invoiceId, refundDto))
                    .isInstanceOf(InvalidRefundException.class);
        }
    }
}
