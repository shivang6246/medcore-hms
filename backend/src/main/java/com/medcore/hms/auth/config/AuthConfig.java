package com.medcore.hms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides shared authentication beans.
 * BCryptPasswordEncoder is used throughout the auth module for password hashing
 * and verification — passwords are NEVER stored in plain text.
 */
@Configuration
public class AuthConfig {

    /**
     * BCrypt with default strength (10 rounds).
     * Increase to 12 for production if latency allows.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
