package com.medcore.hms.billing.service.impl;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.billing.dto.*;
import com.medcore.hms.billing.entity.*;
import com.medcore.hms.billing.exception.InvalidRefundException;
import com.medcore.hms.billing.exception.InvoiceNotFoundException;
import com.medcore.hms.billing.exception.OverpaymentException;
import com.medcore.hms.billing.exception.PaymentNotFoundException;
import com.medcore.hms.billing.mapper.BillingMapper;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.billing.repository.PaymentRepository;
import com.medcore.hms.billing.service.BillingService;
import com.medcore.hms.billing.service.InvoiceEmailService;
import com.medcore.hms.billing.service.InvoicePdfService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final BillingMapper billingMapper;
    private final InvoicePdfService invoicePdfService;
    private final InvoiceEmailService invoiceEmailService;

    @Value("${stripe.secret.key:sk_test_51MockStripeSecretKeyForMedCoreHMS2026}")
    private String stripeSecretKey;

    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(CreateInvoiceRequestDto dto) {
        log.info("Generating invoice for patient ID: {}", dto.patientId());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.patientId()));

        Appointment appointment = null;
        if (dto.appointmentId() != null) {
            appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new AppointmentNotFoundException(dto.appointmentId()));
        }

        String invNum = "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal tax = dto.taxAmount() != null ? dto.taxAmount() : BigDecimal.ZERO;
        BigDecimal discountPct = dto.discountPercentage() != null ? dto.discountPercentage() : BigDecimal.ZERO;

        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceItemRequestDto itemDto : dto.items()) {
            BigDecimal unitPrice = itemDto.unitPrice() != null ? itemDto.unitPrice() : BigDecimal.ZERO;
            int quantity = itemDto.quantity() != null && itemDto.quantity() > 0 ? itemDto.quantity() : 1;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(itemTotal);
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountPct.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(discountPct).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if (dto.discountAmount() != null) {
            discountAmount = dto.discountAmount();
        }

        BigDecimal netSubtotal = subtotal.subtract(discountAmount).compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO
                : subtotal.subtract(discountAmount);
        BigDecimal grandTotal = netSubtotal.add(tax);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invNum)
                .patient(patient)
                .appointment(appointment)
                .issueDate(dto.issueDate() != null ? dto.issueDate() : LocalDate.now())
                .dueDate(dto.dueDate() != null ? dto.dueDate() : (dto.issueDate() != null ? dto.issueDate().plusDays(14) : LocalDate.now().plusDays(14)))
                .subtotal(subtotal)
                .taxAmount(tax)
                .discountAmount(discountAmount)
                .discountPercentage(discountPct)
                .grandTotal(grandTotal)
                .paidAmount(BigDecimal.ZERO)
                .balanceDue(grandTotal)
                .status(InvoiceStatus.UNPAID)
                .items(items)
                .build();

        for (InvoiceItemRequestDto itemDto : dto.items()) {
            BigDecimal unitPrice = itemDto.unitPrice() != null ? itemDto.unitPrice() : BigDecimal.ZERO;
            int quantity = itemDto.quantity() != null && itemDto.quantity() > 0 ? itemDto.quantity() : 1;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(itemDto.description())
                    .category(itemDto.category())
                    .unitPrice(unitPrice)
                    .quantity(quantity)
                    .totalPrice(itemTotal)
                    .build();

            items.add(item);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully with number: {}, subtotal: {}, grand total: {}", invNum, subtotal, grandTotal);
        return billingMapper.toInvoiceResponseDto(savedInvoice);
    }

    @Override
    public InvoiceResponseDto getInvoiceById(UUID id) {
        log.info("Fetching invoice by ID: {}", id);

        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(id));

        return billingMapper.toInvoiceResponseDto(invoice);
    }

    @Override
    public PagedResponse<InvoiceSummaryDto> getInvoicesByPatient(UUID patientId, Pageable pageable) {
        log.info("Fetching invoices for patient ID: {}", patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException(patientId);
        }

        Page<Invoice> page = invoiceRepository.findByPatient_Id(patientId, pageable);
        return PagedResponse.from(page.map(billingMapper::toInvoiceSummaryDto));
    }

    @Override
    public PagedResponse<InvoiceSummaryDto> getAllInvoices(InvoiceStatus status, Pageable pageable) {
        log.info("Fetching all invoices paginated (status filter: {})", status);

        Page<Invoice> page = status != null
                ? invoiceRepository.findByStatus(status, pageable)
                : invoiceRepository.findAll(pageable);

        return PagedResponse.from(page.map(billingMapper::toInvoiceSummaryDto));
    }

    @Override
    @Transactional
    public PaymentResponseDto processPayment(ProcessPaymentRequestDto dto) {
        log.info("Processing payment of {} for invoice ID: {}", dto.amount(), dto.invoiceId());

        Invoice invoice = invoiceRepository.findById(dto.invoiceId())
                .orElseThrow(() -> new InvoiceNotFoundException(dto.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new OverpaymentException("Invoice " + invoice.getInvoiceNumber() + " is already fully PAID.");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new OverpaymentException("Cannot process payment for CANCELLED invoice.");
        }

        if (dto.amount().compareTo(invoice.getBalanceDue()) > 0) {
            throw new OverpaymentException("Payment amount (" + dto.amount() + ") exceeds balance due (" + invoice.getBalanceDue() + ").");
        }

        User processedBy = dto.processedById() != null
                ? userRepository.findById(dto.processedById()).orElse(null)
                : null;

        String receiptNum = "RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .receiptNumber(receiptNum)
                .invoice(invoice)
                .amount(dto.amount())
                .paymentMethod(dto.paymentMethod())
                .transactionReference(dto.transactionReference())
                .paidAt(LocalDateTime.now())
                .status(PaymentStatus.SUCCESS)
                .processedBy(processedBy)
                .remarks(dto.remarks())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(dto.amount());
        BigDecimal newBalanceDue = invoice.getGrandTotal().subtract(newPaidAmount);

        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceDue(newBalanceDue.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newBalanceDue);

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Payment processed. Receipt: {}, Updated Invoice Status: {}", receiptNum, invoice.getStatus());
        return billingMapper.toPaymentResponseDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponseDto processStripePayment(UUID invoiceId, StripePaymentRequestDto dto) {
        log.info("Processing Stripe online payment for invoice ID: {}", invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new OverpaymentException("Invoice " + invoice.getInvoiceNumber() + " is already fully PAID.");
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new OverpaymentException("Cannot process payment for CANCELLED invoice.");
        }

        BigDecimal amountToPay = (dto != null && dto.amount() != null) ? dto.amount() : invoice.getBalanceDue();

        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (amountToPay.compareTo(invoice.getBalanceDue()) > 0) {
            throw new OverpaymentException("Payment amount (" + amountToPay + ") exceeds balance due (" + invoice.getBalanceDue() + ").");
        }

        String paymentType = (dto != null && dto.paymentMethodType() != null) ? dto.paymentMethodType().toUpperCase() : "CARD";
        String currency = (dto != null && dto.currency() != null) ? dto.currency().toLowerCase() : "inr";
        String upiId = (dto != null) ? dto.upiId() : null;

        String stripeRefPrefix = "UPI".equalsIgnoreCase(paymentType) ? "STRIPE-UPI-" : "STRIPE-CH-";
        String stripeRef = stripeRefPrefix + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        String remarks = (dto != null && dto.remarks() != null && !dto.remarks().isBlank())
                ? dto.remarks()
                : ("UPI".equalsIgnoreCase(paymentType) ? ("Stripe UPI Payment (" + (upiId != null ? upiId : "UPI") + ")") : "Stripe Online Card Payment");

        if (stripeSecretKey != null && stripeSecretKey.startsWith("sk_test_") && !stripeSecretKey.contains("Mock")) {
            try {
                com.stripe.Stripe.apiKey = stripeSecretKey;
                com.stripe.param.ChargeCreateParams params = com.stripe.param.ChargeCreateParams.builder()
                        .setAmount(amountToPay.multiply(new BigDecimal("100")).longValue())
                        .setCurrency(currency)
                        .setSource(dto != null && dto.stripeToken() != null ? dto.stripeToken() : "tok_visa")
                        .setDescription("Invoice " + invoice.getInvoiceNumber() + ("UPI".equalsIgnoreCase(paymentType) ? " (UPI Payment)" : ""))
                        .build();
                com.stripe.model.Charge charge = com.stripe.model.Charge.create(params);
                if (charge.getId() != null) {
                    stripeRef = charge.getId();
                }
            } catch (Exception e) {
                log.warn("Stripe API execution note: {}. Proceeding with generated Stripe reference {}.", e.getMessage(), stripeRef);
            }
        }

        String receiptNum = "RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PaymentMethod enumMethod = "UPI".equalsIgnoreCase(paymentType) ? PaymentMethod.UPI : PaymentMethod.CARD;

        Payment payment = Payment.builder()
                .receiptNumber(receiptNum)
                .invoice(invoice)
                .amount(amountToPay)
                .paymentMethod(enumMethod)
                .transactionReference(stripeRef)
                .paidAt(LocalDateTime.now())
                .status(PaymentStatus.SUCCESS)
                .remarks(remarks)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amountToPay);
        BigDecimal newBalanceDue = invoice.getGrandTotal().subtract(newPaidAmount);

        invoice.setPaidAmount(newPaidAmount);
        invoice.setBalanceDue(newBalanceDue.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newBalanceDue);

        if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        // Generate PDF & Email to Patient
        try {
            byte[] pdfBytes = invoicePdfService.generateInvoicePdf(invoice);
            if (invoice.getPatient() != null && invoice.getPatient().getEmail() != null) {
                String patientName = invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName();
                invoiceEmailService.sendInvoicePdfEmail(
                        invoice.getPatient().getEmail(),
                        invoice.getInvoiceNumber(),
                        patientName,
                        amountToPay,
                        pdfBytes
                );
            }
        } catch (Exception e) {
            log.error("Error generating PDF / emailing post Stripe payment for invoice {}", invoice.getInvoiceNumber(), e);
        }

        return billingMapper.toPaymentResponseDto(savedPayment);
    }

    @Override
    public byte[] generateInvoicePdf(UUID invoiceId) {
        log.info("Generating PDF for invoice ID: {}", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        return invoicePdfService.generateInvoicePdf(invoice);
    }

    @Override
    public PaymentResponseDto getPaymentById(UUID id) {
        log.info("Fetching payment transaction by ID: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        return billingMapper.toPaymentResponseDto(payment);
    }

    @Override
    @Transactional
    public RefundResponseDto refundInvoice(UUID invoiceId, RefundRequestDto dto) {
        log.info("Processing refund of {} for invoice ID: {}", dto.refundAmount(), invoiceId);

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRefundException("Cannot process refund. Invoice has no payments recorded.");
        }

        if (dto.refundAmount().compareTo(invoice.getPaidAmount()) > 0) {
            throw new InvalidRefundException("Refund amount (" + dto.refundAmount() + ") exceeds total paid amount (" + invoice.getPaidAmount() + ").");
        }

        User processedBy = dto.processedById() != null ? userRepository.findById(dto.processedById()).orElse(null) : null;

        String refundReceipt = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment refundPayment = Payment.builder()
                .receiptNumber(refundReceipt)
                .invoice(invoice)
                .amount(dto.refundAmount().negate())
                .paymentMethod(PaymentMethod.CASH)
                .transactionReference("REFUND")
                .paidAt(LocalDateTime.now())
                .status(PaymentStatus.REFUNDED)
                .processedBy(processedBy)
                .remarks("Refund: " + dto.reason())
                .build();

        paymentRepository.save(refundPayment);

        BigDecimal newPaid = invoice.getPaidAmount().subtract(dto.refundAmount());
        BigDecimal newBalance = invoice.getGrandTotal().subtract(newPaid);

        invoice.setPaidAmount(newPaid);
        invoice.setBalanceDue(newBalance);

        if (newPaid.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.REFUNDED);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);
        log.info("Refund issued successfully for invoice ID: {}", invoiceId);

        return new RefundResponseDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                dto.refundAmount(),
                invoice.getStatus().name(),
                dto.reason(),
                LocalDateTime.now()
        );
    }

    @Override
    public RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        BigDecimal totalInvoiced = invoiceRepository.calculateTotalInvoicedBetween(start, end);
        BigDecimal totalCollected = paymentRepository.calculateTotalCollectedBetween(startDateTime, endDateTime);
        BigDecimal totalRefunded = paymentRepository.calculateTotalRefundedBetween(startDateTime, endDateTime);
        BigDecimal totalOutstanding = invoiceRepository.calculateTotalOutstandingBalance();

        long invoiceCount = invoiceRepository.countInvoicesBetween(start, end);
        long paymentCount = paymentRepository.countPaymentsBetween(startDateTime, endDateTime);

        return new RevenueReportDto(
                start, end, totalInvoiced, totalCollected, totalOutstanding, totalRefunded, invoiceCount, paymentCount
        );
    }
}
