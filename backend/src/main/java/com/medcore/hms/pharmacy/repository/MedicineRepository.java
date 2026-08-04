package com.medcore.hms.pharmacy.repository;

import com.medcore.hms.pharmacy.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

    @Query("SELECT m FROM Medicine m WHERE m.stockQuantity <= m.reorderLevel AND m.isActive = true")
    Page<Medicine> findLowStockMedicines(Pageable pageable);

    Page<Medicine> findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(String name, String genericName, Pageable pageable);

    boolean existsByNameAndBrand(String name, String brand);
}
