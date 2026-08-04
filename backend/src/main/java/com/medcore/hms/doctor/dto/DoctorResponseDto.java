package com.medcore.hms.doctor.dto;

import com.medcore.hms.doctor.entity.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorResponseDto(
        UUID id,
        UUID userId,
        String employeeId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Gender gender,
        LocalDate dateOfBirth,
        String licenseNumber,
        String specialization,
        String qualification,
        Integer yearsOfExperience,
        BigDecimal consultationFee,
        String profileImageUrl,
        String biography,
        Boolean isActive,
        Boolean isAvailable,
        HospitalRefDto hospital,
        DepartmentRefDto department,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
