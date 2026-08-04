package com.medcore.hms.billing.dto;

import com.medcore.hms.billing.entity.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Line item request detail in an invoice")
public record InvoiceItemRequestDto(
        @NotBlank(message = "Description is required")
        @Schema(description = "Item description", example = "Cardiology Consultation Fee")
        String description,

        @NotNull(message = "Item category is required")
        @Schema(description = "Category", example = "CONSULTATION")
        ItemCategory category,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.00", message = "Unit price cannot be negative")
        @Schema(description = "Unit price", example = "150.00")
        BigDecimal unitPrice,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity", example = "1")
        Integer quantity
) {}
