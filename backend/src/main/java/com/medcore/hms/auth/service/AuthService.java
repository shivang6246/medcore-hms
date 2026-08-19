package com.medcore.hms.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.dto.AuthResponseDto;
import com.medcore.hms.auth.dto.LoginRequestDto;
import com.medcore.hms.auth.dto.MeResponseDto;
import com.medcore.hms.auth.dto.MessageResponseDto;
import com.medcore.hms.auth.dto.PendingRegistrationDto;
import com.medcore.hms.auth.dto.RegisterRequestDto;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.util.DuplicateEmailException;
import com.medcore.hms.auth.util.EmailAlreadyVerifiedException;
import com.medcore.hms.auth.util.EmailNotVerifiedException;
import com.medcore.hms.auth.util.InvalidCredentialsException;
import com.medcore.hms.auth.util.OtpExpiredException;
import com.medcore.hms.email.service.EmailService;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Core authentication service.
 *
 * <p>
 * Handles:
 * <ul>
 * <li>{@link #register} — validate → check duplicate → hash password → store pending in Redis → send OTP</li>
 * <li>{@link #login} — load user → verify password → update lastLoginAt → return JWT + refresh token</li>
 * <li>{@link #verifyEmail} — verify OTP → load pending from Redis → persist User in DB → return JWT + refresh token</li>
 * <li>{@link #refresh} — validate refresh token in Redis → rotate → return new JWT + refresh token pair</li>
 * <li>{@link #logout} — invalidate refresh token in Redis</li>
 * <li>{@link #getCurrentUser} — load user profile by email from SecurityContext</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String PENDING_REGISTER_PREFIX = "pending_register::";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final RoleProfileProvisioner roleProfileProvisioner;

    // -------------------------------------------------------------------------
    // Register
    // -------------------------------------------------------------------------

    /**
     * Initiates registration by saving pending details in Redis and sending a verification OTP.
     * User record is created/updated in DB only after OTP is verified.
     * Re-registering an existing verified email sends a new OTP and updates the password on verify.
     */
    @Transactional
    public MessageResponseDto register(RegisterRequestDto dto) {
        User existingUser = userRepository.findByEmail(dto.email()).orElse(null);
        if (existingUser != null && !Boolean.TRUE.equals(existingUser.getIsEmailVerified())) {
            // Remove incomplete signup so a fresh pending registration can complete
            userRepository.delete(existingUser);
            userRepository.flush();
        }

        RoleName requestedRole = resolvePublicRegisterRole(dto.roleName());
        roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new IllegalStateException(
                        "Role " + requestedRole + " not found. Run DataSeeder first."));

        PendingRegistrationDto pendingDto = new PendingRegistrationDto(
                dto.firstName(),
                dto.lastName(),
                dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.phone(),
                requestedRole.name()
        );

        try {
            String json = objectMapper.writeValueAsString(pendingDto);
            redisTemplate.opsForValue().set(PENDING_REGISTER_PREFIX + dto.email(), json, 15, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize pending registration for {}", dto.email(), e);
            throw new RuntimeException("Failed to process registration request", e);
        }

        String otp = otpService.generateOtp();
        otpService.saveOtp(dto.email(), otp);
        emailService.sendOtpEmail(dto.email(), dto.firstName(), otp);

        log.info("Pending registration created & OTP sent for email: {}", dto.email());

        return new MessageResponseDto(
                "Verification code sent to " + dto.email() + ". Please verify OTP within 15 minutes to complete registration.");
    }

    // -------------------------------------------------------------------------
    // Login
    // -------------------------------------------------------------------------

    /**
     * Authenticates a user and returns a JWT + refresh token pair.
     *
     * @throws InvalidCredentialsException if email not found or password does not match
     */
    @Transactional
    public AuthResponseDto login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.getIsEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        try {
            roleProfileProvisioner.ensureProfiles(user);
        } catch (Exception ex) {
            log.warn("Role profile backfill failed for {}: {}", user.getEmail(), ex.getMessage());
        }
        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDto verifyEmail(String email, String otp) {
        // Validate without consuming — so a later failure can still be retried with the same OTP
        otpService.assertOtpValid(email, otp);

        String pendingJson = redisTemplate.opsForValue().get(PENDING_REGISTER_PREFIX + email);
        User user;

        if (pendingJson != null) {
            try {
                PendingRegistrationDto pendingDto = objectMapper.readValue(pendingJson, PendingRegistrationDto.class);
                RoleName requestedRole = resolveRoleName(pendingDto.roleName());
                Role role = roleRepository.findByName(requestedRole)
                        .orElseThrow(() -> new IllegalStateException("Role " + requestedRole + " not found."));

                user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                    user = User.builder()
                            .firstName(pendingDto.firstName())
                            .lastName(pendingDto.lastName())
                            .email(pendingDto.email())
                            .passwordHash(pendingDto.passwordHash())
                            .phone(pendingDto.phone())
                            .roles(new HashSet<>(Set.of(role)))
                            .isActive(true)
                            .isEmailVerified(true)
                            .build();
                } else {
                    // Existing account (re-register / password update via OTP)
                    user.setFirstName(pendingDto.firstName());
                    user.setLastName(pendingDto.lastName());
                    user.setPasswordHash(pendingDto.passwordHash());
                    user.setPhone(pendingDto.phone());
                    user.setRoles(new HashSet<>(Set.of(role)));
                    user.setIsActive(true);
                    user.setIsEmailVerified(true);
                    user.setDeletedAt(null);
                }

                user = userRepository.save(user);
                roleProfileProvisioner.ensureProfiles(user);
                redisTemplate.delete(PENDING_REGISTER_PREFIX + email);
                otpService.consumeOtp(email);
                log.info("Registration completed for: {}", user.getEmail());
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize pending registration for {}", email, e);
                throw new RuntimeException("Error completing registration", e);
            }
        } else {
            // Fallback for legacy unverified user records in DB
            user = userRepository.findByEmail(email)
                    .orElseThrow(OtpExpiredException::new);

            if (Boolean.TRUE.equals(user.getIsEmailVerified())) {
                throw new EmailAlreadyVerifiedException();
            }

            user.setIsEmailVerified(true);
            user.setIsActive(true);
            user = userRepository.save(user);
            roleProfileProvisioner.ensureProfiles(user);
            otpService.consumeOtp(email);
            log.info("Legacy email verified for: {}", email);
        }

        AuthResponseDto response = buildAuthResponse(user);
        emailService.sendWelcomeEmail(email, user.getFirstName());
        return response;
    }

    public MessageResponseDto resendOtp(String email) {
        String pendingJson = redisTemplate.opsForValue().get(PENDING_REGISTER_PREFIX + email);
        User user = userRepository.findByEmail(email).orElse(null);

        if (pendingJson == null && (user == null || Boolean.TRUE.equals(user.getIsEmailVerified()))) {
            if (user != null && Boolean.TRUE.equals(user.getIsEmailVerified())) {
                throw new EmailAlreadyVerifiedException();
            }
            throw new UsernameNotFoundException("No pending registration found for email: " + email);
        }

        String firstName = "User";
        if (pendingJson != null) {
            try {
                PendingRegistrationDto pendingDto = objectMapper.readValue(pendingJson, PendingRegistrationDto.class);
                firstName = pendingDto.firstName();
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse pending registration for firstName extraction", e);
            }
        } else if (user != null) {
            firstName = user.getFirstName();
        }

        String otp = otpService.resendOtp(email);
        emailService.sendOtpEmail(email, firstName, otp);
        log.info("OTP resent to: {}", email);

        return new MessageResponseDto("A new verification code has been sent to " + email);
    }

    // -------------------------------------------------------------------------
    // Refresh Token
    // -------------------------------------------------------------------------

    /**
     * Validates a refresh token, rotates it, and returns a new access + refresh
     * token pair.
     *
     * @param refreshToken the opaque refresh token UUID from the client
     * @throws InvalidCredentialsException if the token is not found / expired in
     *                                     Redis
     */
    public AuthResponseDto refresh(String refreshToken) {

        // 1. Validate and rotate — deletes the old token from Redis
        String email = refreshTokenService.validateAndExtractEmail(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token presented");
                    return new InvalidCredentialsException();
                });

        // 2. Load user from DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        log.info("Token refreshed for: {}", email);

        return buildAuthResponse(user);
    }

    // -------------------------------------------------------------------------
    // Logout
    // -------------------------------------------------------------------------

    /**
     * Invalidates the supplied refresh token in Redis, effectively logging out the
     * session.
     *
     * @param refreshToken the refresh token to revoke
     */
    public void logout(String refreshToken) {
        refreshTokenService.invalidate(refreshToken);
        log.info("Refresh token invalidated (logout)");
    }

    // -------------------------------------------------------------------------
    // Current User
    // -------------------------------------------------------------------------

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param email email extracted from the SecurityContext (JWT subject)
     * @throws UsernameNotFoundException if the user is not found
     */
    @Transactional(readOnly = true)
    public MeResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        UUID patientId = patientRepository.findByEmail(email)
                .map(p -> p.getId())
                .orElse(null);

        UUID doctorId = doctorRepository.findByUser_Id(user.getId())
                .map(d -> d.getId())
                .orElse(null);

        return new MeResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                roleNames,
                user.getIsActive(),
                user.getHospital() != null ? user.getHospital().getId() : null,
                user.getHospital() != null ? user.getHospital().getName() : null,
                patientId,
                doctorId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Public self-registration is limited to PATIENT and DOCTOR.
     * Staff/admin accounts must be created by hospital admins / seeders.
     */
    private RoleName resolvePublicRegisterRole(String roleName) {
        RoleName resolved = resolveRoleName(roleName);
        if (resolved != RoleName.PATIENT && resolved != RoleName.DOCTOR) {
            throw new IllegalArgumentException(
                    "Self-registration is only allowed for PATIENT or DOCTOR. Other roles must be assigned by an admin.");
        }
        return resolved;
    }

    private RoleName resolveRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return RoleName.PATIENT;
        }
        try {
            return RoleName.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown role '{}' requested — defaulting to PATIENT", roleName);
            return RoleName.PATIENT;
        }
    }

    /**
     * Central factory: generates a new access token + refresh token pair for the
     * given user.
     */
    private AuthResponseDto buildAuthResponse(User user) {
        var springUser = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(springUser);
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        AuthResponseDto.UserSummary summary = new AuthResponseDto.UserSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());

        return AuthResponseDto.of(accessToken, 86400L, newRefreshToken, summary);
    }
}
