package com.medcore.hms.auth.config;

import com.medcore.hms.auth.jwt.JwtAuthenticationFilter;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * <p>
 * Design decisions:
 * <ul>
 * <li>CSRF disabled — stateless REST API with JWT.</li>
 * <li>Session management set to STATELESS — no server-side session state.</li>
 * <li>{@code /api/auth/**} is fully public; everything else requires
 * authentication.</li>
 * <li>JWT filter runs before Spring's built-in
 * {@link UsernamePasswordAuthenticationFilter}.</li>
 * <li>{@code @EnableMethodSecurity} enables {@code @PreAuthorize} /
 * {@code @PostAuthorize}
 * for fine-grained RBAC on individual controller methods.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final com.medcore.hms.auth.jwt.JwtAuthenticationEntryPoint authEntryPoint;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Security Filter Chain
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/health",
                                "/api/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        // All other endpoints require a valid JWT
                        .anyRequest().authenticated())

                // Return 401 Unauthorized for unauthenticated requests
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))

                // Stateless session — no HttpSession created or used
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Wire in our custom DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // JWT filter runs before the standard form-login filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Authentication Infrastructure Beans
    // -------------------------------------------------------------------------

    /**
     * DaoAuthenticationProvider wires our {@link CustomUserDetailsService} with
     * BCrypt.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean (used by Spring Security internals and
     * optionally by services that need programmatic authentication).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
