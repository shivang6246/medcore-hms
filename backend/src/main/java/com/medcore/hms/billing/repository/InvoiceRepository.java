package com.medcore.hms.billing.repository;

import com.medcore.hms.billing.entity.Invoice;
import com.medcore.hms.billing.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByPatient_Id(UUID patientId, Pageable pageable);

    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate AND i.status != 'CANCELLED'")
    BigDecimal calculateTotalInvoicedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.status IN ('UNPAID', 'PARTIALLY_PAID')")
    BigDecimal calculateTotalOutstandingBalance();

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.issueDate BETWEEN :startDate AND :endDate")
    long countInvoicesBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
