package com.medcore.hms.billing.repository;

import com.medcore.hms.billing.entity.Payment;
import com.medcore.hms.billing.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByInvoice_Id(UUID invoiceId);

    Page<Payment> findByInvoice_Id(UUID invoiceId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paidAt BETWEEN :startDateTime AND :endDateTime")
    BigDecimal calculateTotalCollectedBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'REFUNDED' AND p.paidAt BETWEEN :startDateTime AND :endDateTime")
    BigDecimal calculateTotalRefundedBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.paidAt BETWEEN :startDateTime AND :endDateTime")
    long countPaymentsBetween(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);
}
