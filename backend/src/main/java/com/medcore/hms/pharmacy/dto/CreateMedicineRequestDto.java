package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload to create a new medicine entry")
public record CreateMedicineRequestDto(
        @NotBlank(message = "Medicine name is required")
        @Schema(description = "Commercial name", example = "Paracetamol 500mg")
        String name,

        @Schema(description = "Generic chemical name", example = "Acetaminophen")
        String genericName,

        @Schema(description = "Brand name", example = "Calpol")
        String brand,

        @NotBlank(message = "Category is required")
        @Schema(description = "Medicine category", example = "Analgesics")
        String category,

        @Schema(description = "Strength/concentration", example = "500mg")
        String strength,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        @Schema(description = "Selling price per unit", example = "5.50")
        BigDecimal unitPrice,

        @Schema(description = "Manufacturer name", example = "GSK Pharmaceuticals")
        String manufacturer,

        @Schema(description = "Barcode / UPC code", example = "8901234567890")
        String barcode,

        @Min(value = 0, message = "Reorder level cannot be negative")
        @Schema(description = "Reorder threshold quantity", example = "20")
        Integer reorderLevel
) {}
