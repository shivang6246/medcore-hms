package com.medcore.hms.doctor.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DoctorSummaryDto(
        UUID id,
        String employeeId,
        String fullName,
        String email,
        String specialization,
        String departmentName,
        String hospitalName,
        BigDecimal consultationFee,
        Boolean isActive
) {}
