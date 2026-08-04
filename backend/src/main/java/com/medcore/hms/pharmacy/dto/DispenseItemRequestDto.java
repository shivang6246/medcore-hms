package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Item detail in a dispense request")
public record DispenseItemRequestDto(
        @NotNull(message = "Medicine ID is required")
        @Schema(description = "Medicine UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID medicineId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(description = "Quantity to dispense", example = "10")
        Integer quantity
) {}
