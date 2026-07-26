package com.medcore.hms.auth.service;

import com.medcore.hms.auth.util.OtpExpiredException;
import com.medcore.hms.auth.util.OtpMismatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String KEY_PREFIX = "otp::";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${otp.expiration-minutes}")
    private long expirationMinutes;

    public String generateOtp() {
        int otp = 100_000 + RANDOM.nextInt(900_000);
        return String.valueOf(otp);
    }

    public void saveOtp(String email, String otp) {
        redisTemplate.opsForValue().set(key(email), otp, expirationMinutes, TimeUnit.MINUTES);
        log.debug("OTP saved for: {} (expires in {} min)", email, expirationMinutes);
    }

    public void verifyOtp(String email, String submittedOtp) {
        String stored = redisTemplate.opsForValue().get(key(email));
        if (stored == null) {
            throw new OtpExpiredException();
        }
        if (!stored.equals(submittedOtp)) {
            throw new OtpMismatchException();
        }
        deleteOtp(email);
    }

    public String resendOtp(String email) {
        deleteOtp(email);
        String otp = generateOtp();
        saveOtp(email, otp);
        return otp;
    }

    public void deleteOtp(String email) {
        redisTemplate.delete(key(email));
    }

    private String key(String email) {
        return KEY_PREFIX + email;
    }
}
