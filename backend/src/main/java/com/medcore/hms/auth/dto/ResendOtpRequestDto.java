package com.medcore.hms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequestDto(

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
