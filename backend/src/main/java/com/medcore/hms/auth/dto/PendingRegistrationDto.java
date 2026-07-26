package com.medcore.hms.auth.dto;

/**
 * Payload held temporarily in Redis while waiting for email OTP verification.
 */
public record PendingRegistrationDto(
        String firstName,
        String lastName,
        String email,
        String passwordHash,
        String phone,
        String roleName
) {}
