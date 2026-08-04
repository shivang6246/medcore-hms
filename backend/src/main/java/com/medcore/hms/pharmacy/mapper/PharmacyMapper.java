package com.medcore.hms.pharmacy.mapper;

import com.medcore.hms.pharmacy.dto.*;
import com.medcore.hms.pharmacy.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PharmacyMapper {

    public MedicineResponseDto toMedicineResponseDto(Medicine m) {
        if (m == null) return null;
        boolean lowStock = m.getStockQuantity() <= m.getReorderLevel();
        return new MedicineResponseDto(
                m.getId(),
                m.getName(),
                m.getGenericName(),
                m.getBrand(),
                m.getCategory(),
                m.getStrength(),
                m.getUnitPrice(),
                m.getManufacturer(),
                m.getBarcode(),
                m.getStockQuantity(),
                m.getReorderLevel(),
                lowStock,
                m.isActive(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }

    public MedicineSummaryDto toMedicineSummaryDto(Medicine m) {
        if (m == null) return null;
        boolean lowStock = m.getStockQuantity() <= m.getReorderLevel();
        return new MedicineSummaryDto(
                m.getId(),
                m.getName(),
                m.getCategory(),
                m.getStrength(),
                m.getUnitPrice(),
                m.getStockQuantity(),
                lowStock,
                m.isActive()
        );
    }

    public SupplierResponseDto toSupplierResponseDto(Supplier s) {
        if (s == null) return null;
        return new SupplierResponseDto(
                s.getId(),
                s.getName(),
                s.getContactPerson(),
                s.getPhone(),
                s.getEmail(),
                s.getAddress(),
                s.getLicenseNumber(),
                s.isActive(),
                s.getCreatedAt()
        );
    }

    public MedicineBatchResponseDto toMedicineBatchResponseDto(MedicineBatch b) {
        if (b == null) return null;
        boolean expired = b.getExpiryDate().isBefore(LocalDate.now());
        String supplierName = b.getSupplier() != null ? b.getSupplier().getName() : null;
        return new MedicineBatchResponseDto(
                b.getId(),
                b.getMedicine() != null ? b.getMedicine().getId() : null,
                b.getMedicine() != null ? b.getMedicine().getName() : null,
                supplierName,
                b.getBatchNumber(),
                b.getExpiryDate(),
                b.getPurchasePrice(),
                b.getSellingPrice(),
                b.getInitialQuantity(),
                b.getCurrentQuantity(),
                expired,
                b.isActive(),
                b.getCreatedAt()
        );
    }

    public DispenseRecordResponseDto toDispenseRecordResponseDto(DispenseRecord r) {
        if (r == null) return null;

        String patientName = r.getPatient() != null
                ? r.getPatient().getFirstName() + " " + r.getPatient().getLastName()
                : "Unknown Patient";

        String doctorName = (r.getDoctor() != null && r.getDoctor().getUser() != null)
                ? r.getDoctor().getUser().getFirstName() + " " + r.getDoctor().getUser().getLastName()
                : null;

        String pharmacistName = r.getPharmacist() != null
                ? r.getPharmacist().getFirstName() + " " + r.getPharmacist().getLastName()
                : null;

        List<DispenseRecordResponseDto.DispenseItemResponseDto> itemDtos = r.getItems() != null
                ? r.getItems().stream().map(i -> new DispenseRecordResponseDto.DispenseItemResponseDto(
                        i.getId(),
                        i.getMedicine().getId(),
                        i.getMedicine().getName(),
                        i.getBatch() != null ? i.getBatch().getBatchNumber() : "N/A",
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getTotalPrice()
                )).toList()
                : List.of();

        return new DispenseRecordResponseDto(
                r.getId(),
                r.getDispenseNumber(),
                r.getPatient() != null ? r.getPatient().getId() : null,
                patientName,
                doctorName,
                pharmacistName,
                r.getTotalAmount(),
                r.getDispensedAt(),
                itemDtos,
                r.getRemarks()
        );
    }

    public StockTransactionResponseDto toStockTransactionResponseDto(StockTransaction t) {
        if (t == null) return null;
        String perfName = t.getPerformedBy() != null
                ? t.getPerformedBy().getFirstName() + " " + t.getPerformedBy().getLastName()
                : null;
        return new StockTransactionResponseDto(
                t.getId(),
                t.getMedicine() != null ? t.getMedicine().getName() : null,
                t.getBatch() != null ? t.getBatch().getBatchNumber() : null,
                t.getTransactionType(),
                t.getQuantity(),
                t.getQuantityAfter(),
                t.getTransactionDate(),
                perfName,
                t.getReason()
        );
    }
}
