package com.medcore.hms.pharmacy.service.impl;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.pharmacy.dto.*;
import com.medcore.hms.pharmacy.entity.*;
import com.medcore.hms.pharmacy.exception.InsufficientStockException;
import com.medcore.hms.pharmacy.exception.MedicineNotFoundException;
import com.medcore.hms.pharmacy.exception.SupplierNotFoundException;
import com.medcore.hms.pharmacy.mapper.PharmacyMapper;
import com.medcore.hms.pharmacy.repository.*;
import com.medcore.hms.prescription.entity.Prescription;
import com.medcore.hms.prescription.repository.PrescriptionRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import com.medcore.hms.pharmacy.service.PharmacyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PharmacyServiceImpl implements PharmacyService {

    private final MedicineRepository medicineRepository;
    private final SupplierRepository supplierRepository;
    private final MedicineBatchRepository medicineBatchRepository;
    private final DispenseRecordRepository dispenseRecordRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final UserRepository userRepository;
    private final PharmacyMapper pharmacyMapper;

    @Override
    @Transactional
    public MedicineResponseDto createMedicine(CreateMedicineRequestDto dto) {
        log.info("Creating medicine master entry: '{}', category: '{}'", dto.name(), dto.category());

        int defaultReorder = dto.reorderLevel() != null ? dto.reorderLevel() : 10;

        Medicine medicine = Medicine.builder()
                .name(dto.name())
                .genericName(dto.genericName())
                .brand(dto.brand())
                .category(dto.category())
                .strength(dto.strength())
                .unitPrice(dto.unitPrice())
                .manufacturer(dto.manufacturer())
                .barcode(dto.barcode())
                .reorderLevel(defaultReorder)
                .stockQuantity(0)
                .isActive(true)
                .build();

        Medicine savedMedicine = medicineRepository.save(medicine);
        log.info("Medicine created with ID: {}", savedMedicine.getId());
        return pharmacyMapper.toMedicineResponseDto(savedMedicine);
    }

    @Override
    @Transactional
    public MedicineResponseDto updateMedicine(UUID id, UpdateMedicineRequestDto dto) {
        log.info("Updating medicine ID: {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));

        medicine.setName(dto.name());
        medicine.setGenericName(dto.genericName());
        medicine.setBrand(dto.brand());
        medicine.setCategory(dto.category());
        medicine.setStrength(dto.strength());
        medicine.setUnitPrice(dto.unitPrice());
        medicine.setManufacturer(dto.manufacturer());
        medicine.setBarcode(dto.barcode());
        if (dto.reorderLevel() != null) {
            medicine.setReorderLevel(dto.reorderLevel());
        }

        Medicine updatedMedicine = medicineRepository.save(medicine);
        return pharmacyMapper.toMedicineResponseDto(updatedMedicine);
    }

    @Override
    public MedicineResponseDto getMedicineById(UUID id) {
        log.info("Fetching medicine by ID: {}", id);

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new MedicineNotFoundException(id));

        return pharmacyMapper.toMedicineResponseDto(medicine);
    }

    @Override
    public PagedResponse<MedicineSummaryDto> getAllMedicines(String search, Pageable pageable) {
        log.info("Fetching medicines paginated (search: '{}')", search);

        Page<Medicine> page = (search != null && !search.isBlank())
                ? medicineRepository.findByNameContainingIgnoreCaseOrGenericNameContainingIgnoreCase(search, search, pageable)
                : medicineRepository.findAll(pageable);

        return PagedResponse.from(page.map(pharmacyMapper::toMedicineSummaryDto));
    }

    @Override
    @Transactional
    public SupplierResponseDto createSupplier(CreateSupplierRequestDto dto) {
        log.info("Creating supplier: '{}'", dto.name());

        Supplier supplier = Supplier.builder()
                .name(dto.name())
                .contactPerson(dto.contactPerson())
                .phone(dto.phone())
                .email(dto.email())
                .address(dto.address())
                .licenseNumber(dto.licenseNumber())
                .isActive(true)
                .build();

        Supplier savedSupplier = supplierRepository.save(supplier);
        return pharmacyMapper.toSupplierResponseDto(savedSupplier);
    }

    @Override
    public PagedResponse<SupplierResponseDto> getAllSuppliers(Pageable pageable) {
        log.info("Fetching all suppliers paginated");

        Page<Supplier> page = supplierRepository.findAll(pageable);
        return PagedResponse.from(page.map(pharmacyMapper::toSupplierResponseDto));
    }

    @Override
    @Transactional
    public MedicineBatchResponseDto addStock(AddStockBatchRequestDto dto) {
        log.info("Adding stock for medicine ID: {}, batch: '{}', qty: {}", dto.medicineId(), dto.batchNumber(), dto.quantity());

        Medicine medicine = medicineRepository.findById(dto.medicineId())
                .orElseThrow(() -> new MedicineNotFoundException(dto.medicineId()));

        Supplier supplier = null;
        if (dto.supplierId() != null) {
            supplier = supplierRepository.findById(dto.supplierId())
                    .orElseThrow(() -> new SupplierNotFoundException(dto.supplierId()));
        }

        User performedBy = null;
        if (dto.performedById() != null) {
            performedBy = userRepository.findById(dto.performedById()).orElse(null);
        }

        MedicineBatch batch = MedicineBatch.builder()
                .medicine(medicine)
                .supplier(supplier)
                .batchNumber(dto.batchNumber())
                .expiryDate(dto.expiryDate())
                .purchasePrice(dto.purchasePrice())
                .sellingPrice(dto.sellingPrice())
                .initialQuantity(dto.quantity())
                .currentQuantity(dto.quantity())
                .isActive(true)
                .build();

        MedicineBatch savedBatch = medicineBatchRepository.save(batch);

        // Update medicine master total stock quantity
        medicine.setStockQuantity(medicine.getStockQuantity() + dto.quantity());
        medicineRepository.save(medicine);

        // Audit log stock transaction
        StockTransaction tx = StockTransaction.builder()
                .medicine(medicine)
                .batch(savedBatch)
                .transactionType(TransactionType.PURCHASE)
                .quantity(dto.quantity())
                .quantityAfter(medicine.getStockQuantity())
                .transactionDate(LocalDateTime.now())
                .performedBy(performedBy)
                .reason("Batch Purchase stock added — " + dto.batchNumber())
                .build();
        stockTransactionRepository.save(tx);

        log.info("Stock batch added successfully. New total stock for '{}': {}", medicine.getName(), medicine.getStockQuantity());
        return pharmacyMapper.toMedicineBatchResponseDto(savedBatch);
    }

    @Override
    @Transactional
    public DispenseRecordResponseDto dispense(DispenseRequestDto dto) {
        log.info("Dispensing medicine order for patient ID: {}", dto.patientId());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.patientId()));

        Doctor doctor = dto.doctorId() != null ? doctorRepository.findById(dto.doctorId()).orElse(null) : null;
        Prescription prescription = dto.prescriptionId() != null ? prescriptionRepository.findById(dto.prescriptionId()).orElse(null) : null;
        User pharmacist = dto.pharmacistId() != null ? userRepository.findById(dto.pharmacistId()).orElse(null) : null;

        String dispenseNum = "DISP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        DispenseRecord record = DispenseRecord.builder()
                .dispenseNumber(dispenseNum)
                .patient(patient)
                .doctor(doctor)
                .prescription(prescription)
                .pharmacist(pharmacist)
                .totalAmount(BigDecimal.ZERO)
                .dispensedAt(LocalDateTime.now())
                .remarks(dto.remarks())
                .build();

        DispenseRecord savedRecord = dispenseRecordRepository.save(record);

        BigDecimal grandTotal = BigDecimal.ZERO;
        List<DispenseItem> dispenseItems = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (DispenseItemRequestDto itemDto : dto.items()) {
            Medicine medicine = medicineRepository.findById(itemDto.medicineId())
                    .orElseThrow(() -> new MedicineNotFoundException(itemDto.medicineId()));

            if (medicine.getStockQuantity() < itemDto.quantity()) {
                throw new InsufficientStockException("Insufficient total stock for medicine: '" + medicine.getName() + "'. Required: " + itemDto.quantity() + ", Available: " + medicine.getStockQuantity());
            }

            // FEFO: First Expiry First Out allocation from valid batches
            List<MedicineBatch> validBatches = medicineBatchRepository.findAvailableValidBatchesFEFO(medicine.getId(), today);
            int remainingToDispense = itemDto.quantity();

            for (MedicineBatch batch : validBatches) {
                if (remainingToDispense <= 0) break;

                int deduct = Math.min(remainingToDispense, batch.getCurrentQuantity());
                batch.setCurrentQuantity(batch.getCurrentQuantity() - deduct);
                medicineBatchRepository.save(batch);

                BigDecimal itemTotal = batch.getSellingPrice().multiply(BigDecimal.valueOf(deduct));
                grandTotal = grandTotal.add(itemTotal);

                DispenseItem item = DispenseItem.builder()
                        .dispenseRecord(savedRecord)
                        .medicine(medicine)
                        .batch(batch)
                        .quantity(deduct)
                        .unitPrice(batch.getSellingPrice())
                        .totalPrice(itemTotal)
                        .build();

                dispenseItems.add(item);
                remainingToDispense -= deduct;
            }

            if (remainingToDispense > 0) {
                throw new InsufficientStockException("Insufficient unexpired stock for medicine: '" + medicine.getName() + "'. Remaining unfulfilled: " + remainingToDispense);
            }

            // Deduct stock from medicine total
            medicine.setStockQuantity(medicine.getStockQuantity() - itemDto.quantity());
            medicineRepository.save(medicine);

            // Audit transaction
            StockTransaction tx = StockTransaction.builder()
                    .medicine(medicine)
                    .transactionType(TransactionType.DISPENSED)
                    .quantity(-itemDto.quantity())
                    .quantityAfter(medicine.getStockQuantity())
                    .transactionDate(LocalDateTime.now())
                    .performedBy(pharmacist)
                    .reason("Dispensed under receipt " + dispenseNum)
                    .build();
            stockTransactionRepository.save(tx);
        }

        savedRecord.setTotalAmount(grandTotal);
        savedRecord.setItems(dispenseItems);
        DispenseRecord finalRecord = dispenseRecordRepository.save(savedRecord);

        log.info("Dispense completed with receipt: {}, total amount: {}", dispenseNum, grandTotal);
        return pharmacyMapper.toDispenseRecordResponseDto(finalRecord);
    }

    @Override
    public PagedResponse<MedicineSummaryDto> getLowStockMedicines(Pageable pageable) {
        log.info("Fetching low-stock medicines");

        Page<Medicine> page = medicineRepository.findLowStockMedicines(pageable);
        return PagedResponse.from(page.map(pharmacyMapper::toMedicineSummaryDto));
    }

    @Override
    public PagedResponse<MedicineBatchResponseDto> getExpiredBatches(Pageable pageable) {
        log.info("Fetching expired medicine batches");

        Page<MedicineBatch> page = medicineBatchRepository.findExpiredBatches(LocalDate.now(), pageable);
        return PagedResponse.from(page.map(pharmacyMapper::toMedicineBatchResponseDto));
    }

    @Override
    @Transactional
    public StockTransactionResponseDto adjustStock(StockAdjustmentRequestDto dto) {
        log.info("Adjusting stock for medicine ID: {}, delta: {}", dto.medicineId(), dto.quantityChange());

        Medicine medicine = medicineRepository.findById(dto.medicineId())
                .orElseThrow(() -> new MedicineNotFoundException(dto.medicineId()));

        int newTotal = medicine.getStockQuantity() + dto.quantityChange();
        if (newTotal < 0) {
            throw new InsufficientStockException("Stock adjustment results in negative quantity for medicine: " + medicine.getName());
        }

        MedicineBatch batch = null;
        if (dto.batchId() != null) {
            batch = medicineBatchRepository.findById(dto.batchId()).orElse(null);
            if (batch != null) {
                int newBatchQty = batch.getCurrentQuantity() + dto.quantityChange();
                if (newBatchQty < 0) {
                    throw new InsufficientStockException("Batch stock adjustment results in negative quantity for batch: " + batch.getBatchNumber());
                }
                batch.setCurrentQuantity(newBatchQty);
                medicineBatchRepository.save(batch);
            }
        }

        medicine.setStockQuantity(newTotal);
        medicineRepository.save(medicine);

        User performedBy = dto.performedById() != null ? userRepository.findById(dto.performedById()).orElse(null) : null;

        StockTransaction tx = StockTransaction.builder()
                .medicine(medicine)
                .batch(batch)
                .transactionType(dto.transactionType())
                .quantity(dto.quantityChange())
                .quantityAfter(newTotal)
                .transactionDate(LocalDateTime.now())
                .performedBy(performedBy)
                .reason(dto.reason())
                .build();

        StockTransaction savedTx = stockTransactionRepository.save(tx);
        return pharmacyMapper.toStockTransactionResponseDto(savedTx);
    }

    @Override
    public PagedResponse<StockTransactionResponseDto> getStockTransactions(UUID medicineId, Pageable pageable) {
        log.info("Fetching stock transactions for medicine ID: {}", medicineId);

        Page<StockTransaction> page = medicineId != null
                ? stockTransactionRepository.findByMedicine_Id(medicineId, pageable)
                : stockTransactionRepository.findAll(pageable);

        return PagedResponse.from(page.map(pharmacyMapper::toStockTransactionResponseDto));
    }
}
