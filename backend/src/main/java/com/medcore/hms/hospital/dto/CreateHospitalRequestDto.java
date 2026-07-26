package com.medcore.hms.hospital.dto;

import com.medcore.hms.common.dto.AddressDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateHospitalRequestDto(

        @NotBlank(message = "Hospital name is required")
        @Size(max = 200, message = "Hospital name must be at most 200 characters")
        String name,

        @NotBlank(message = "Registration number is required")
        @Size(max = 100, message = "Registration number must be at most 100 characters")
        String registrationNumber,

        @NotBlank(message = "License number is required")
        @Size(max = 100, message = "License number must be at most 100 characters")
        String licenseNumber,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
        @Size(max = 20, message = "Phone must be at most 20 characters")
        String phone,

        @Size(max = 255, message = "Website URL must be at most 255 characters")
        String website,

        String description,

        @Size(max = 255, message = "Logo URL must be at most 255 characters")
        String logoUrl,

        @Valid
        AddressDto address
) {}
