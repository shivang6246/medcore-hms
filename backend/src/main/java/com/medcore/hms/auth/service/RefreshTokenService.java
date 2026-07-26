package com.medcore.hms.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages refresh token lifecycle using Redis as the backing store.
 *
 * <p>Token format: opaque UUID string (not a JWT).
 *
 * <p>Redis key schema:
 * <pre>
 *   KEY:   "refreshToken::{tokenUuid}"
 *   VALUE: "{user-email}"
 *   TTL:   jwt.refresh-expiration-ms (default 7 days)
 * </pre>
 *
 * <p>Token rotation: each call to {@link #validateAndExtractEmail} deletes the
 * consumed token from Redis — the caller is responsible for issuing a new pair.
 * This prevents replay attacks; a stolen token can only be used once.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refreshToken::";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a new refresh token for the given email and persists it in Redis.
     *
     * @param email the authenticated user's email
     * @return opaque refresh token UUID string
     */
    public String createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                key(token),
                email,
                refreshExpirationMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("Refresh token created for: {}", email);
        return token;
    }

    /**
     * Validates a refresh token by looking it up in Redis, then <strong>deletes it</strong>
     * (rotation — the caller must issue a new pair immediately).
     *
     * @param token the refresh token to validate
     * @return the email associated with the token, or empty if not found / expired
     */
    public Optional<String> validateAndExtractEmail(String token) {
        String redisKey = key(token);
        String email    = redisTemplate.opsForValue().get(redisKey);

        if (email == null) {
            log.warn("Refresh token not found or expired in Redis");
            return Optional.empty();
        }

        // Rotate: delete the consumed token immediately
        redisTemplate.delete(redisKey);
        log.debug("Refresh token rotated for: {}", email);
        return Optional.of(email);
    }

    /**
     * Invalidates a refresh token (used on logout).
     * Silent no-op if the token does not exist.
     *
     * @param token the refresh token to invalidate
     */
    public void invalidate(String token) {
        Boolean deleted = redisTemplate.delete(key(token));
        if (Boolean.TRUE.equals(deleted)) {
            log.debug("Refresh token invalidated (logout)");
        } else {
            log.debug("Refresh token not found during logout — already expired or invalid");
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String key(String token) {
        return KEY_PREFIX + token;
    }
}
