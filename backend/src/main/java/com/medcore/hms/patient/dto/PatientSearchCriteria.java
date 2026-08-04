package com.medcore.hms.patient.dto;

import com.medcore.hms.patient.entity.BloodGroup;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public record PatientSearchCriteria(
        UUID hospitalId,
        String name,
        String phone,
        String email,
        String patientId,
        BloodGroup bloodGroup,
        Boolean isActive
) {}
