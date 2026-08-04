package com.medcore.hms.patient.dto;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.doctor.dto.HospitalRefDto;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.patient.entity.BloodGroup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponseDto(
        UUID id,
        String patientId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        BloodGroup bloodGroup,
        String phone,
        String email,
        AddressDto address,
        EmergencyContactDto emergencyContact,
        String insuranceProvider,
        String insurancePolicyNumber,
        String allergies,
        String medicalHistory,
        Boolean isActive,
        HospitalRefDto hospital,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
