package com.medcore.hms.pharmacy.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.pharmacy.dto.*;
import com.medcore.hms.pharmacy.entity.*;
import com.medcore.hms.pharmacy.exception.InsufficientStockException;
import com.medcore.hms.pharmacy.exception.MedicineNotFoundException;
import com.medcore.hms.pharmacy.mapper.PharmacyMapper;
import com.medcore.hms.pharmacy.repository.*;
import com.medcore.hms.pharmacy.service.impl.PharmacyServiceImpl;
import com.medcore.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PharmacyService Unit Tests")
class PharmacyServiceImplTest {

    @Mock private MedicineRepository medicineRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private MedicineBatchRepository medicineBatchRepository;
    @Mock private DispenseRecordRepository dispenseRecordRepository;
    @Mock private StockTransactionRepository stockTransactionRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private UserRepository userRepository;
    @Mock private PharmacyMapper pharmacyMapper;

    @InjectMocks
    private PharmacyServiceImpl pharmacyService;

    private UUID medicineId;
    private UUID patientId;
    private Medicine medicine;
    private CreateMedicineRequestDto createMedicineDto;
    private MedicineResponseDto medicineResponseDto;

    @BeforeEach
    void setUp() {
        medicineId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        medicine = Medicine.builder()
                .name("Amoxicillin")
                .genericName("Amoxicillin Trihydrate")
                .category("Antibiotic")
                .strength("500mg")
                .unitPrice(new BigDecimal("10.00"))
                .stockQuantity(100)
                .reorderLevel(20)
                .isActive(true)
                .build();
        medicine.setId(medicineId);

        createMedicineDto = new CreateMedicineRequestDto(
                "Amoxicillin", "Amoxicillin Trihydrate", "GenericBrand", "Antibiotic", "500mg",
                new BigDecimal("10.00"), "GSK", "123456", 20
        );

        medicineResponseDto = new MedicineResponseDto(
                medicineId, "Amoxicillin", "Amoxicillin Trihydrate", "GenericBrand", "Antibiotic", "500mg",
                new BigDecimal("10.00"), "GSK", "123456", 100, 20, false, true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Medicine Master Catalog Tests")
    class MedicineTests {

        @Test
        @DisplayName("Should successfully create medicine")
        void createMedicine_Success() {
            when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);
            when(pharmacyMapper.toMedicineResponseDto(medicine)).thenReturn(medicineResponseDto);

            MedicineResponseDto result = pharmacyService.createMedicine(createMedicineDto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Amoxicillin");
            verify(medicineRepository).save(any(Medicine.class));
        }

        @Test
        @DisplayName("Should throw MedicineNotFoundException when ID invalid")
        void getMedicineById_NotFound() {
            when(medicineRepository.findById(medicineId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pharmacyService.getMedicineById(medicineId))
                    .isInstanceOf(MedicineNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Stock Replenishment Tests")
    class ReplenishmentTests {

        @Test
        @DisplayName("Should add stock batch and update total medicine quantity")
        void addStock_Success() {
            AddStockBatchRequestDto stockDto = new AddStockBatchRequestDto(
                    medicineId, null, "BATCH-01", LocalDate.now().plusYears(1),
                    new BigDecimal("5.00"), new BigDecimal("10.00"), 50, null
            );

            MedicineBatch batch = MedicineBatch.builder()
                    .medicine(medicine)
                    .batchNumber("BATCH-01")
                    .expiryDate(LocalDate.now().plusYears(1))
                    .purchasePrice(new BigDecimal("5.00"))
                    .sellingPrice(new BigDecimal("10.00"))
                    .initialQuantity(50)
                    .currentQuantity(50)
                    .build();

            MedicineBatchResponseDto batchResponse = new MedicineBatchResponseDto(
                    UUID.randomUUID(), medicineId, "Amoxicillin", null, "BATCH-01",
                    LocalDate.now().plusYears(1), new BigDecimal("5.00"), new BigDecimal("10.00"),
                    50, 50, false, true, LocalDateTime.now()
            );

            when(medicineRepository.findById(medicineId)).thenReturn(Optional.of(medicine));
            when(medicineBatchRepository.save(any(MedicineBatch.class))).thenReturn(batch);
            when(pharmacyMapper.toMedicineBatchResponseDto(batch)).thenReturn(batchResponse);

            MedicineBatchResponseDto result = pharmacyService.addStock(stockDto);

            assertThat(result).isNotNull();
            assertThat(medicine.getStockQuantity()).isEqualTo(150);
            verify(stockTransactionRepository).save(any(StockTransaction.class));
        }
    }

    @Nested
    @DisplayName("Dispense Medicine Tests")
    class DispenseTests {

        @Test
        @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
        void dispense_InsufficientStock() {
            Patient patient = Patient.builder().build();
            patient.setId(patientId);

            DispenseRequestDto dispenseDto = new DispenseRequestDto(
                    patientId, null, null, null,
                    List.of(new DispenseItemRequestDto(medicineId, 200)), "Dispense notes"
            );

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(medicineRepository.findById(medicineId)).thenReturn(Optional.of(medicine));

            assertThatThrownBy(() -> pharmacyService.dispense(dispenseDto))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient total stock");
        }

        @Test
        @DisplayName("Should successfully dispense medicines using FEFO batch deduction")
        void dispense_Success() {
            Patient patient = Patient.builder().firstName("John").lastName("Doe").build();
            patient.setId(patientId);

            MedicineBatch batch = MedicineBatch.builder()
                    .medicine(medicine)
                    .batchNumber("B1")
                    .sellingPrice(new BigDecimal("10.00"))
                    .currentQuantity(50)
                    .expiryDate(LocalDate.now().plusMonths(6))
                    .build();

            DispenseRequestDto dispenseDto = new DispenseRequestDto(
                    patientId, null, null, null,
                    List.of(new DispenseItemRequestDto(medicineId, 10)), "Dispense notes"
            );

            DispenseRecord record = DispenseRecord.builder()
                    .dispenseNumber("DISP-100")
                    .patient(patient)
                    .totalAmount(new BigDecimal("100.00"))
                    .dispensedAt(LocalDateTime.now())
                    .build();

            DispenseRecordResponseDto recordResponse = new DispenseRecordResponseDto(
                    UUID.randomUUID(), "DISP-100", patientId, "John Doe", null, null,
                    new BigDecimal("100.00"), LocalDateTime.now(), List.of(), "Notes"
            );

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(medicineRepository.findById(medicineId)).thenReturn(Optional.of(medicine));
            when(dispenseRecordRepository.save(any(DispenseRecord.class))).thenReturn(record);
            when(medicineBatchRepository.findAvailableValidBatchesFEFO(eq(medicineId), any(LocalDate.class)))
                    .thenReturn(List.of(batch));
            when(pharmacyMapper.toDispenseRecordResponseDto(any(DispenseRecord.class))).thenReturn(recordResponse);

            DispenseRecordResponseDto result = pharmacyService.dispense(dispenseDto);

            assertThat(result).isNotNull();
            assertThat(medicine.getStockQuantity()).isEqualTo(90);
            assertThat(batch.getCurrentQuantity()).isEqualTo(40);
        }
    }
}
