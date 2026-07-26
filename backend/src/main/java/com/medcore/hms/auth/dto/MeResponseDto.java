package com.medcore.hms.auth.dto;

import java.util.Set;
import java.util.UUID;

/**
 * Response body for GET /api/auth/me — the authenticated user's profile.
 */
public record MeResponseDto(

        UUID   id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Set<String> roles,
        Boolean isActive
) {}
