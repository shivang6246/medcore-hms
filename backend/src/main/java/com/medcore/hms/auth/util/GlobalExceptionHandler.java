package com.medcore.hms.auth.util;

import com.medcore.hms.hospital.exception.DuplicateHospitalEmailException;
import com.medcore.hms.hospital.exception.DuplicateRegistrationNumberException;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the Auth module (and general validation errors).
 *
 * <p>Uses RFC 7807 {@link ProblemDetail} responses for consistent error structure:
 * <pre>
 * {
 *   "type":       "https://medcore-hms.com/errors/duplicate-email",
 *   "title":      "Duplicate Email",
 *   "status":     409,
 *   "detail":     "A user with email 'x@y.com' already exists.",
 *   "instance":   "/api/auth/register",
 *   "timestamp":  "2026-07-26T05:00:00Z"
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://medcore-hms.com/errors/";

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        log.warn("Duplicate email: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Email", "duplicate-email", ex.getMessage());
    }

    @ExceptionHandler(HospitalNotFoundException.class)
    public ProblemDetail handleHospitalNotFound(HospitalNotFoundException ex) {
        log.warn("Hospital not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Hospital Not Found", "hospital-not-found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateRegistrationNumberException.class)
    public ProblemDetail handleDuplicateRegNumber(DuplicateRegistrationNumberException ex) {
        log.warn("Duplicate registration number: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Registration Number", "duplicate-registration-number", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.hospital.exception.DuplicateLicenseNumberException.class)
    public ProblemDetail handleDuplicateLicenseNumber(com.medcore.hms.hospital.exception.DuplicateLicenseNumberException ex) {
        log.warn("Duplicate license number: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate License Number", "duplicate-license-number", ex.getMessage());
    }

    @ExceptionHandler(DuplicateHospitalEmailException.class)
    public ProblemDetail handleDuplicateHospitalEmail(DuplicateHospitalEmailException ex) {
        log.warn("Duplicate hospital email: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Hospital Email", "duplicate-hospital-email", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.hospital.exception.HospitalAlreadyExistsException.class)
    public ProblemDetail handleHospitalAlreadyExists(com.medcore.hms.hospital.exception.HospitalAlreadyExistsException ex) {
        log.warn("Hospital already exists: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Hospital Already Exists", "hospital-already-exists", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "Invalid Credentials", "invalid-credentials", ex.getMessage());
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ProblemDetail handleOtpExpired(OtpExpiredException ex) {
        log.warn("OTP expired: {}", ex.getMessage());
        return problem(HttpStatus.GONE, "OTP Expired", "otp-expired", ex.getMessage());
    }

    @ExceptionHandler(OtpMismatchException.class)
    public ProblemDetail handleOtpMismatch(OtpMismatchException ex) {
        log.warn("OTP mismatch: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "OTP Mismatch", "otp-mismatch", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ProblemDetail handleEmailAlreadyVerified(EmailAlreadyVerifiedException ex) {
        log.warn("Email already verified: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Email Already Verified", "email-already-verified", ex.getMessage());
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ProblemDetail handleEmailNotVerified(EmailNotVerifiedException ex) {
        log.warn("Email not verified: {}", ex.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Email Not Verified", "email-not-verified", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (first, second) -> first
                ));
        log.warn("Validation failed: {}", fieldErrors);
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation Error", "validation-error",
                "Request validation failed. Check 'errors' for details.");
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return problem(HttpStatus.FORBIDDEN, "Access Denied", "access-denied", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "unauthorized", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "internal-error",
                "An unexpected error occurred. Please try again later.");
    }

    private ProblemDetail problem(HttpStatus status, String title, String errorSlug, String detail) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detail);
        p.setTitle(title);
        p.setType(URI.create(ERROR_BASE_URI + errorSlug));
        p.setProperty("timestamp", Instant.now());
        return p;
    }
}
