package com.medcore.hms.pharmacy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

@Schema(description = "Request payload to create a new Supplier")
public record CreateSupplierRequestDto(
        @NotBlank(message = "Supplier company name is required")
        @Schema(description = "Company name", example = "MediPharma Distributors")
        String name,

        @Schema(description = "Contact person name", example = "Robert Miller")
        String contactPerson,

        @NotBlank(message = "Phone number is required")
        @Schema(description = "Phone number", example = "+1-555-0199")
        String phone,

        @Schema(description = "Email address", example = "orders@medipharma.com")
        String email,

        @Schema(description = "Physical address", example = "100 Supply Way, Suite 400")
        String address,

        @Schema(description = "Tax / Drug License ID", example = "DL-994827")
        String licenseNumber
) {}
