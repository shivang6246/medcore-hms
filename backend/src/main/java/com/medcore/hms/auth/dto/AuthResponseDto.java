package com.medcore.hms.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Response body returned on successful register, login, or token refresh.
 */
public record AuthResponseDto(

        /** Short-lived JWT access token. */
        String token,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        long expiresIn,

        /** Opaque refresh token (UUID) stored in Redis — use to rotate access tokens. */
        @JsonProperty("refresh_token")
        String refreshToken,

        UserSummary user
) {

    /**
     * Compact user summary embedded in the auth response.
     */
    public record UserSummary(
            UUID   id,
            String firstName,
            String lastName,
            String email
    ) {}

    /** Convenience factory — always sets tokenType to "Bearer". */
    public static AuthResponseDto of(
            String      token,
            long        expiresIn,
            String      refreshToken,
            UserSummary user
    ) {
        return new AuthResponseDto(token, "Bearer", expiresIn, refreshToken, user);
    }
}
