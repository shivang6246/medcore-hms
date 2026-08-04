package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request payload to dispense medicine items to a patient")
public record DispenseRequestDto(
        @NotNull(message = "Patient ID is required")
        @Schema(description = "Patient UUID", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID patientId,

        @Schema(description = "Optional doctor UUID", example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e")
        UUID doctorId,

        @Schema(description = "Optional prescription UUID", example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID prescriptionId,

        @Schema(description = "ID of dispensing pharmacist/user")
        UUID pharmacistId,

        @NotEmpty(message = "Dispense items list cannot be empty")
        @Valid
        @Schema(description = "List of medicines to dispense")
        List<DispenseItemRequestDto> items,

        @Schema(description = "Optional remarks", example = "Dispensed in full")
        String remarks
) {}
