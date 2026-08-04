package com.medcore.hms.pharmacy.repository;

import com.medcore.hms.pharmacy.entity.MedicineBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, UUID> {

    @Query("SELECT b FROM MedicineBatch b WHERE b.expiryDate < :today AND b.currentQuantity > 0")
    Page<MedicineBatch> findExpiredBatches(@Param("today") LocalDate today, Pageable pageable);

    @Query("SELECT b FROM MedicineBatch b WHERE b.medicine.id = :medicineId AND b.currentQuantity > 0 AND b.expiryDate >= :today ORDER BY b.expiryDate ASC")
    List<MedicineBatch> findAvailableValidBatchesFEFO(@Param("medicineId") UUID medicineId, @Param("today") LocalDate today);

    Page<MedicineBatch> findByMedicine_Id(UUID medicineId, Pageable pageable);
}
