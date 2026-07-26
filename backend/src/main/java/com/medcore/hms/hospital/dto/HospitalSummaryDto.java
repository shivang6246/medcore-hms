package com.medcore.hms.hospital.dto;

import java.util.UUID;

public record HospitalSummaryDto(
        UUID    id,
        String  name,
        String  registrationNumber,
        String  licenseNumber,
        String  email,
        String  phone,
        String  logoUrl,
        Boolean isActive
) {}
