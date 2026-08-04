package com.medcore.hms.patient.dto;

public record EmergencyContactDto(
        String name,
        String phone,
        String relationship
) {}
