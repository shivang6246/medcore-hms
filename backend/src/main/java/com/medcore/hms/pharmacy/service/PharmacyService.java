package com.medcore.hms.pharmacy.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.pharmacy.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PharmacyService {

    MedicineResponseDto createMedicine(CreateMedicineRequestDto dto);

    MedicineResponseDto updateMedicine(UUID id, UpdateMedicineRequestDto dto);

    MedicineResponseDto getMedicineById(UUID id);

    PagedResponse<MedicineSummaryDto> getAllMedicines(String search, Pageable pageable);

    SupplierResponseDto createSupplier(CreateSupplierRequestDto dto);

    PagedResponse<SupplierResponseDto> getAllSuppliers(Pageable pageable);

    MedicineBatchResponseDto addStock(AddStockBatchRequestDto dto);

    DispenseRecordResponseDto dispense(DispenseRequestDto dto);

    PagedResponse<MedicineSummaryDto> getLowStockMedicines(Pageable pageable);

    PagedResponse<MedicineBatchResponseDto> getExpiredBatches(Pageable pageable);

    StockTransactionResponseDto adjustStock(StockAdjustmentRequestDto dto);

    PagedResponse<StockTransactionResponseDto> getStockTransactions(UUID medicineId, Pageable pageable);
}
