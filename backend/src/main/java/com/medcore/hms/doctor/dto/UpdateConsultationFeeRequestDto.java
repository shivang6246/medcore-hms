package com.medcore.hms.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateConsultationFeeRequestDto(
        @NotNull(message = "Fee is required")
        @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative")
        @Digits(integer = 8, fraction = 2, message = "Fee must have up to 8 digits and 2 decimal places")
        BigDecimal fee
) {}
