package com.medcore.hms.billing.repository;

import com.medcore.hms.billing.entity.*;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Billing Repositories — Integration Tests")
class BillingRepositoryTest {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private PatientRepository patientRepository;

    private Patient patient;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("General Clinic " + UUID.randomUUID().toString().substring(0, 5))
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("genclinic_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Bruce")
                .lastName("Wayne")
                .dateOfBirth(LocalDate.of(1982, 4, 19))
                .phone("98765432" + (int)(Math.random()*100))
                .email("bruce_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("Alfred Pennyworth")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        invoice = invoiceRepository.save(Invoice.builder()
                .invoiceNumber("INV-TEST-001")
                .patient(patient)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .subtotal(new BigDecimal("500.00"))
                .taxAmount(new BigDecimal("50.00"))
                .discountAmount(BigDecimal.ZERO)
                .grandTotal(new BigDecimal("550.00"))
                .paidAmount(BigDecimal.ZERO)
                .balanceDue(new BigDecimal("550.00"))
                .status(InvoiceStatus.UNPAID)
                .build());
    }

    @Test
    @DisplayName("Should find invoices by Patient ID")
    void findByPatient_Id_Success() {
        Page<Invoice> page = invoiceRepository.findByPatient_Id(patient.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getInvoiceNumber()).isEqualTo("INV-TEST-001");
    }

    @Test
    @DisplayName("Should process payment and calculate total collected between dates")
    void calculateTotalCollected_Success() {
        paymentRepository.save(Payment.builder()
                .receiptNumber("RCP-TEST-1")
                .invoice(invoice)
                .amount(new BigDecimal("300.00"))
                .paymentMethod(PaymentMethod.CASH)
                .paidAt(LocalDateTime.now())
                .status(PaymentStatus.SUCCESS)
                .build());

        BigDecimal totalCollected = paymentRepository.calculateTotalCollectedBetween(
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)
        );

        assertThat(totalCollected).isEqualByComparingTo("300.00");
    }
}
