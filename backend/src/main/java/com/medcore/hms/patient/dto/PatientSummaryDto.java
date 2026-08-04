package com.medcore.hms.patient.dto;

import com.medcore.hms.patient.entity.BloodGroup;

import java.time.LocalDate;
import java.util.UUID;

public record PatientSummaryDto(
        UUID id,
        String patientId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String phone,
        String email,
        BloodGroup bloodGroup,
        Boolean isActive,
        String hospitalName
) {}
