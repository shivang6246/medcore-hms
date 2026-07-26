package com.medcore.hms.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.dto.AuthResponseDto;
import com.medcore.hms.auth.dto.MessageResponseDto;
import com.medcore.hms.auth.dto.PendingRegistrationDto;
import com.medcore.hms.auth.dto.RegisterRequestDto;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.util.DuplicateEmailException;
import com.medcore.hms.email.service.EmailService;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void register_ShouldStorePendingRegistrationInRedisAndSendOtp() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john.doe@example.com", "password123", "1234567890", "PATIENT"
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        Role role = Role.builder().name(RoleName.PATIENT).description("Patient role").build();
        when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(otpService.generateOtp()).thenReturn("123456");

        MessageResponseDto response = authService.register(dto);

        assertNotNull(response);
        assertTrue(response.message().contains("Verification code sent"));

        verify(valueOperations).set(eq("pending_register::john.doe@example.com"), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        verify(otpService).saveOtp("john.doe@example.com", "123456");
        verify(emailService).sendOtpEmail("john.doe@example.com", "John", "123456");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyVerified() {
        RegisterRequestDto dto = new RegisterRequestDto(
                "John", "Doe", "john.doe@example.com", "password123", "1234567890", "PATIENT"
        );
        User verifiedUser = User.builder().email(dto.email()).isEmailVerified(true).build();

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.of(verifiedUser));

        assertThrows(DuplicateEmailException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifyEmail_ShouldCreateUserInDB_WhenPendingRegistrationExists() throws Exception {
        String email = "john.doe@example.com";
        String otp = "123456";

        PendingRegistrationDto pendingDto = new PendingRegistrationDto(
                "John", "Doe", email, "encodedPassword", "1234567890", "PATIENT"
        );
        String pendingJson = objectMapper.writeValueAsString(pendingDto);

        when(valueOperations.get("pending_register::" + email)).thenReturn(pendingJson);
        Role patientRole = Role.builder().name(RoleName.PATIENT).description("Patient role").build();
        when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));

        User savedUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .passwordHash("encodedPassword")
                .phone("1234567890")
                .roles(Set.of(patientRole))
                .isActive(true)
                .isEmailVerified(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(mock(UserDetails.class));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(email)).thenReturn("refresh-token");

        AuthResponseDto response = authService.verifyEmail(email, otp);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("refresh-token", response.refreshToken());

        verify(otpService).verifyOtp(email, otp);
        verify(userRepository).save(any(User.class));
        verify(redisTemplate).delete("pending_register::" + email);
        verify(emailService).sendWelcomeEmail(email, "John");
    }
}
