package com.medcore.hms.pharmacy.repository;

import com.medcore.hms.pharmacy.entity.Medicine;
import com.medcore.hms.pharmacy.entity.MedicineBatch;
import com.medcore.hms.pharmacy.entity.Supplier;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Pharmacy Repositories — Integration Tests")
class PharmacyRepositoryTest {

    @Autowired private MedicineRepository medicineRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private MedicineBatchRepository medicineBatchRepository;

    private Medicine medicine;
    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = supplierRepository.save(Supplier.builder()
                .name("PharmaDist " + UUID.randomUUID().toString().substring(0, 5))
                .phone("1234567890")
                .isActive(true)
                .build());

        medicine = medicineRepository.save(Medicine.builder()
                .name("Ibuprofen " + UUID.randomUUID().toString().substring(0, 5))
                .category("Painkiller")
                .unitPrice(new BigDecimal("8.00"))
                .stockQuantity(5)
                .reorderLevel(15)
                .isActive(true)
                .build());

        medicineBatchRepository.save(MedicineBatch.builder()
                .medicine(medicine)
                .supplier(supplier)
                .batchNumber("BATCH-TEST-1")
                .expiryDate(LocalDate.now().plusMonths(6))
                .purchasePrice(new BigDecimal("4.00"))
                .sellingPrice(new BigDecimal("8.00"))
                .initialQuantity(50)
                .currentQuantity(5)
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Should detect low-stock medicines")
    void findLowStockMedicines_Success() {
        Page<Medicine> page = medicineRepository.findLowStockMedicines(PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).getId()).isEqualTo(medicine.getId());
    }

    @Test
    @DisplayName("Should find available valid batches sorted by FEFO (First Expiry First Out)")
    void findAvailableValidBatchesFEFO_Success() {
        List<MedicineBatch> batches = medicineBatchRepository.findAvailableValidBatchesFEFO(medicine.getId(), LocalDate.now());

        assertThat(batches).hasSize(1);
        assertThat(batches.get(0).getBatchNumber()).isEqualTo("BATCH-TEST-1");
    }

    @Test
    @DisplayName("Should find expired batches")
    void findExpiredBatches_Success() {
        // Save expired batch
        medicineBatchRepository.save(MedicineBatch.builder()
                .medicine(medicine)
                .batchNumber("EXPIRED-BATCH")
                .expiryDate(LocalDate.now().minusDays(10))
                .purchasePrice(new BigDecimal("4.00"))
                .sellingPrice(new BigDecimal("8.00"))
                .initialQuantity(20)
                .currentQuantity(10)
                .isActive(true)
                .build());

        Page<MedicineBatch> page = medicineBatchRepository.findExpiredBatches(LocalDate.now(), PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().stream().anyMatch(b -> b.getBatchNumber().equals("EXPIRED-BATCH"))).isTrue();
    }
}
