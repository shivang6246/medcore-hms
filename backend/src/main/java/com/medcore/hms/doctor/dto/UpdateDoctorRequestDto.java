package com.medcore.hms.doctor.dto;

import com.medcore.hms.doctor.entity.Gender;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDoctorRequestDto(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
        @Size(max = 20)
        String phone,

        Gender gender,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 150)
        String specialization,

        @Size(max = 255)
        String qualification,

        @Min(value = 0, message = "Years of experience cannot be negative")
        @Max(value = 60, message = "Years of experience cannot exceed 60")
        Integer yearsOfExperience,

        @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative")
        @Digits(integer = 8, fraction = 2)
        BigDecimal consultationFee,

        @Size(max = 500)
        @Pattern(regexp = "^(https?://.*)?$", message = "Profile image URL must be a valid http or https URL")
        String profileImageUrl,

        @Size(max = 5000)
        String biography
) {}
