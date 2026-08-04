package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response DTO for a Supplier entity")
public record SupplierResponseDto(
        @Schema(description = "Supplier UUID")
        UUID id,

        @Schema(description = "Company name")
        String name,

        @Schema(description = "Contact person name")
        String contactPerson,

        @Schema(description = "Phone number")
        String phone,

        @Schema(description = "Email address")
        String email,

        @Schema(description = "Address")
        String address,

        @Schema(description = "License number")
        String licenseNumber,

        @Schema(description = "Active status")
        boolean isActive,

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt
) {}
