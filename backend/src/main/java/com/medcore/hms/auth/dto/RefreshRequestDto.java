package com.medcore.hms.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/auth/refresh.
 */
public record RefreshRequestDto(

        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
