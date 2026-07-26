package com.medcore.hms.auth.controller;

import com.medcore.hms.auth.dto.AuthResponseDto;
import com.medcore.hms.auth.dto.LoginRequestDto;
import com.medcore.hms.auth.dto.MeResponseDto;
import com.medcore.hms.auth.dto.MessageResponseDto;
import com.medcore.hms.auth.dto.RefreshRequestDto;
import com.medcore.hms.auth.dto.RegisterRequestDto;
import com.medcore.hms.auth.dto.ResendOtpRequestDto;
import com.medcore.hms.auth.dto.VerifyEmailRequestDto;
import com.medcore.hms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> register(
            @Valid @RequestBody RegisterRequestDto dto) {
        log.info("Registration request for email: {}", dto.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto dto) {
        log.info("Login request for email: {}", dto.email());
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponseDto> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto dto) {
        log.info("Email verification for: {}", dto.email());
        return ResponseEntity.ok(authService.verifyEmail(dto.email(), dto.otp()));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponseDto> resendOtp(
            @Valid @RequestBody ResendOtpRequestDto dto) {
        log.info("OTP resend request for: {}", dto.email());
        return ResponseEntity.ok(authService.resendOtp(dto.email()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshRequestDto dto) {
        return ResponseEntity.ok(authService.refresh(dto.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshRequestDto dto) {
        authService.logout(dto.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MeResponseDto> me(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(authService.getCurrentUser(currentUser.getUsername()));
    }
}

