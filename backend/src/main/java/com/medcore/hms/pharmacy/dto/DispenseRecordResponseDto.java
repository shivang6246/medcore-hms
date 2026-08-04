package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Response DTO for Dispense Record")
public record DispenseRecordResponseDto(
        @Schema(description = "Dispense record UUID")
        UUID id,

        @Schema(description = "Dispense receipt/reference number")
        String dispenseNumber,

        @Schema(description = "Patient UUID")
        UUID patientId,

        @Schema(description = "Patient name")
        String patientName,

        @Schema(description = "Doctor name if provided")
        String doctorName,

        @Schema(description = "Pharmacist name")
        String pharmacistName,

        @Schema(description = "Total bill amount")
        BigDecimal totalAmount,

        @Schema(description = "Dispensed timestamp")
        LocalDateTime dispensedAt,

        @Schema(description = "Dispensed line items")
        List<DispenseItemResponseDto> items,

        @Schema(description = "Remarks")
        String remarks
) {
    public record DispenseItemResponseDto(
            UUID id,
            UUID medicineId,
            String medicineName,
            String batchNumber,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {}
}
