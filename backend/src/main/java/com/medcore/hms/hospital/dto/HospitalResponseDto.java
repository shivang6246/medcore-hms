package com.medcore.hms.hospital.dto;

import com.medcore.hms.common.dto.AddressDto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalResponseDto(
        UUID          id,
        String        name,
        String        registrationNumber,
        String        licenseNumber,
        String        email,
        String        phone,
        String        website,
        String        description,
        String        logoUrl,
        Boolean       isActive,
        AddressDto    address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
