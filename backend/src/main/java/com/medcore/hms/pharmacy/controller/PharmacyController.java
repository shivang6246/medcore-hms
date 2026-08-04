package com.medcore.hms.pharmacy.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.pharmacy.dto.*;
import com.medcore.hms.pharmacy.service.PharmacyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Pharmacy & Inventory Management", description = "REST APIs for medicine catalog, stock replenishment, inventory audits, low-stock alerts, and medicine dispensing.")
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PharmacyController {

    private final PharmacyService pharmacyService;

    // ── Medicine Master APIs ─────────────────────────────────────────────────

    @Operation(summary = "Create medicine catalog entry")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Medicine created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/api/medicines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> createMedicine(
            @Valid @RequestBody CreateMedicineRequestDto dto) {
        log.info("REST request to create medicine: {}", dto.name());
        MedicineResponseDto response = pharmacyService.createMedicine(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medicine created successfully"));
    }

    @Operation(summary = "Get all medicines (paginated, with search)")
    @GetMapping("/api/medicines")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicineSummaryDto>>> getAllMedicines(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        log.info("REST request to fetch medicines paginated (search: {})", search);
        PagedResponse<MedicineSummaryDto> response = pharmacyService.getAllMedicines(search, PageRequest.of(page, size, parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(response, "Medicines fetched successfully"));
    }

    @Operation(summary = "Get medicine by ID")
    @GetMapping("/api/medicines/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR')")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> getMedicineById(
            @Parameter(description = "Medicine UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch medicine ID: {}", id);
        MedicineResponseDto response = pharmacyService.getMedicineById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine fetched successfully"));
    }

    @Operation(summary = "Update medicine details")
    @PutMapping("/api/medicines/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineResponseDto>> updateMedicine(
            @Parameter(description = "Medicine UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicineRequestDto dto) {
        log.info("REST request to update medicine ID: {}", id);
        MedicineResponseDto response = pharmacyService.updateMedicine(id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Medicine updated successfully"));
    }

    // ── Inventory Stock APIs ─────────────────────────────────────────────────

    @Operation(summary = "Add stock batch (inventory replenishment)")
    @PostMapping("/api/inventory/add-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<MedicineBatchResponseDto>> addStock(
            @Valid @RequestBody AddStockBatchRequestDto dto) {
        log.info("REST request to add stock batch for medicine ID: {}", dto.medicineId());
        MedicineBatchResponseDto response = pharmacyService.addStock(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock batch added successfully"));
    }

    @Operation(summary = "Dispense medicine order")
    @PostMapping("/api/dispense")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<DispenseRecordResponseDto>> dispense(
            @Valid @RequestBody DispenseRequestDto dto) {
        log.info("REST request to dispense medicines to patient ID: {}", dto.patientId());
        DispenseRecordResponseDto response = pharmacyService.dispense(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Medicines dispensed successfully"));
    }

    @Operation(summary = "Get low stock medicines")
    @GetMapping("/api/inventory/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicineSummaryDto>>> getLowStockMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch low-stock medicines");
        PagedResponse<MedicineSummaryDto> response = pharmacyService.getLowStockMedicines(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response, "Low-stock medicines fetched successfully"));
    }

    @Operation(summary = "Get expired medicine batches")
    @GetMapping("/api/inventory/expired")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PagedResponse<MedicineBatchResponseDto>>> getExpiredBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch expired medicine batches");
        PagedResponse<MedicineBatchResponseDto> response = pharmacyService.getExpiredBatches(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response, "Expired batches fetched successfully"));
    }

    @Operation(summary = "Manual stock adjustment / audit entry")
    @PostMapping("/api/inventory/adjust")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<StockTransactionResponseDto>> adjustStock(
            @Valid @RequestBody StockAdjustmentRequestDto dto) {
        log.info("REST request to adjust stock for medicine ID: {}", dto.medicineId());
        StockTransactionResponseDto response = pharmacyService.adjustStock(dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock adjusted successfully"));
    }

    @Operation(summary = "Get stock transaction history")
    @GetMapping("/api/inventory/transactions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PagedResponse<StockTransactionResponseDto>>> getStockTransactions(
            @RequestParam(required = false) UUID medicineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch stock transactions (medicineId filter: {})", medicineId);
        PagedResponse<StockTransactionResponseDto> response = pharmacyService.getStockTransactions(
                medicineId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate")));
        return ResponseEntity.ok(ApiResponse.success(response, "Stock transactions fetched successfully"));
    }

    // ── Supplier APIs ────────────────────────────────────────────────────────

    @Operation(summary = "Create supplier entry")
    @PostMapping("/api/suppliers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<SupplierResponseDto>> createSupplier(
            @Valid @RequestBody CreateSupplierRequestDto dto) {
        log.info("REST request to create supplier: {}", dto.name());
        SupplierResponseDto response = pharmacyService.createSupplier(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Supplier created successfully"));
    }

    @Operation(summary = "Get all suppliers (paginated)")
    @GetMapping("/api/suppliers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST')")
    public ResponseEntity<ApiResponse<PagedResponse<SupplierResponseDto>>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch suppliers paginated");
        PagedResponse<SupplierResponseDto> response = pharmacyService.getAllSuppliers(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response, "Suppliers fetched successfully"));
    }

    // ── Private Helpers ──────────────────────────────────────────────────────

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.ASC, "name");
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, property);
    }
}
