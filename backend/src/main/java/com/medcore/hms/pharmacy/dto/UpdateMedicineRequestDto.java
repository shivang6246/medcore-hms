package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload to update an existing medicine entry")
public record UpdateMedicineRequestDto(
        @NotBlank(message = "Medicine name is required")
        @Schema(description = "Updated name", example = "Paracetamol Extra 500mg")
        String name,

        @Schema(description = "Updated generic name", example = "Acetaminophen")
        String genericName,

        @Schema(description = "Updated brand", example = "Calpol Extra")
        String brand,

        @NotBlank(message = "Category is required")
        @Schema(description = "Updated category", example = "Analgesics & Antipyretics")
        String category,

        @Schema(description = "Updated strength", example = "500mg")
        String strength,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Schema(description = "Updated unit price", example = "6.00")
        BigDecimal unitPrice,

        @Schema(description = "Updated manufacturer", example = "GSK Pharmaceuticals")
        String manufacturer,

        @Schema(description = "Updated barcode", example = "8901234567890")
        String barcode,

        @Min(value = 0, message = "Reorder level cannot be negative")
        @Schema(description = "Updated reorder level", example = "25")
        Integer reorderLevel
) {}
