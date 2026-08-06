package com.medcore.hms.auth.util;

import com.medcore.hms.department.exception.DepartmentNotFoundException;
import com.medcore.hms.doctor.exception.*;
import com.medcore.hms.doctor.schedule.exception.ScheduleConflictException;
import com.medcore.hms.doctor.schedule.exception.ScheduleNotFoundException;
import com.medcore.hms.doctor.slot.exception.SlotAlreadyBookedException;
import com.medcore.hms.doctor.slot.exception.SlotGenerationException;
import com.medcore.hms.doctor.slot.exception.SlotNotFoundException;
import com.medcore.hms.hospital.exception.DuplicateHospitalEmailException;
import com.medcore.hms.hospital.exception.DuplicateRegistrationNumberException;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.patient.exception.DuplicatePatientEmailException;
import com.medcore.hms.patient.exception.DuplicatePatientPhoneException;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.exception.AppointmentStatusException;
import com.medcore.hms.appointment.exception.AppointmentConflictException;
import com.medcore.hms.appointment.exception.SlotNotAvailableException;
import com.medcore.hms.medicalrecord.exception.AppointmentMismatchException;
import com.medcore.hms.medicalrecord.exception.DuplicateMedicalRecordException;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.prescription.exception.PrescriptionNotFoundException;
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

    @ExceptionHandler(DoctorNotFoundException.class)
    public ProblemDetail handleDoctorNotFound(DoctorNotFoundException ex) {
        log.warn("Doctor not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Doctor Not Found", "doctor-not-found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateDoctorEmailException.class)
    public ProblemDetail handleDuplicateDoctorEmail(DuplicateDoctorEmailException ex) {
        log.warn("Duplicate doctor email: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Doctor Email", "duplicate-doctor-email", ex.getMessage());
    }

    @ExceptionHandler(DuplicateLicenseNumberException.class)
    public ProblemDetail handleDuplicateLicense(DuplicateLicenseNumberException ex) {
        log.warn("Duplicate license number: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate License Number", "duplicate-license-number", ex.getMessage());
    }

    @ExceptionHandler(DuplicateEmployeeIdException.class)
    public ProblemDetail handleDuplicateEmployeeId(DuplicateEmployeeIdException ex) {
        log.warn("Duplicate employee ID: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Employee ID", "duplicate-employee-id", ex.getMessage());
    }

    @ExceptionHandler(DoctorAlreadyExistsException.class)
    public ProblemDetail handleDoctorAlreadyExists(DoctorAlreadyExistsException ex) {
        log.warn("Doctor already exists: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Doctor Already Exists", "doctor-already-exists", ex.getMessage());
    }

    @ExceptionHandler(InvalidDepartmentAssignmentException.class)
    public ProblemDetail handleInvalidDepartmentAssignment(InvalidDepartmentAssignmentException ex) {
        log.warn("Invalid department assignment: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Invalid Department Assignment", "invalid-department-assignment", ex.getMessage());
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ProblemDetail handleDepartmentNotFound(DepartmentNotFoundException ex) {
        log.warn("Department not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Department Not Found", "department-not-found", ex.getMessage());
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ProblemDetail handleScheduleNotFound(ScheduleNotFoundException ex) {
        log.warn("Schedule not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Schedule Not Found", "schedule-not-found", ex.getMessage());
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ProblemDetail handleScheduleConflict(ScheduleConflictException ex) {
        log.warn("Schedule conflict: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Schedule Conflict", "schedule-conflict", ex.getMessage());
    }

    @ExceptionHandler(SlotNotFoundException.class)
    public ProblemDetail handleSlotNotFound(SlotNotFoundException ex) {
        log.warn("Slot not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Slot Not Found", "slot-not-found", ex.getMessage());
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ProblemDetail handleSlotAlreadyBooked(SlotAlreadyBookedException ex) {
        log.warn("Slot already booked: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Slot Already Booked", "slot-already-booked", ex.getMessage());
    }

    @ExceptionHandler(SlotGenerationException.class)
    public ProblemDetail handleSlotGenerationError(SlotGenerationException ex) {
        log.warn("Slot generation error: {}", ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Slot Generation Error", "slot-generation-error", ex.getMessage());
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(PatientNotFoundException ex) {
        log.warn("Patient not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Patient Not Found", "patient-not-found", ex.getMessage());
    }

    @ExceptionHandler(DuplicatePatientPhoneException.class)
    public ProblemDetail handleDuplicatePatientPhone(DuplicatePatientPhoneException ex) {
        log.warn("Duplicate patient phone: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Patient Phone", "duplicate-patient-phone", ex.getMessage());
    }

    @ExceptionHandler(DuplicatePatientEmailException.class)
    public ProblemDetail handleDuplicatePatientEmail(DuplicatePatientEmailException ex) {
        log.warn("Duplicate patient email: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Patient Email", "duplicate-patient-email", ex.getMessage());
    }

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ProblemDetail handleAppointmentNotFound(AppointmentNotFoundException ex) {
        log.warn("Appointment not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Appointment Not Found", "appointment-not-found", ex.getMessage());
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ProblemDetail handleSlotNotAvailable(SlotNotAvailableException ex) {
        log.warn("Slot not available: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Slot Not Available", "slot-not-available", ex.getMessage());
    }

    @ExceptionHandler(AppointmentStatusException.class)
    public ProblemDetail handleAppointmentStatus(AppointmentStatusException ex) {
        log.warn("Invalid appointment status transition: {}", ex.getMessage());
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid Status Transition", "appointment-status-error", ex.getMessage());
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ProblemDetail handleAppointmentConflict(AppointmentConflictException ex) {
        log.warn("Appointment conflict: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Appointment Conflict", "appointment-conflict", ex.getMessage());
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

    @ExceptionHandler(MedicalRecordNotFoundException.class)
    public ProblemDetail handleMedicalRecordNotFound(MedicalRecordNotFoundException ex) {
        log.warn("Medical record not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Medical Record Not Found", "medical-record-not-found", ex.getMessage());
    }

    @ExceptionHandler(DuplicateMedicalRecordException.class)
    public ProblemDetail handleDuplicateMedicalRecord(DuplicateMedicalRecordException ex) {
        log.warn("Duplicate medical record: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Duplicate Medical Record", "duplicate-medical-record", ex.getMessage());
    }

    @ExceptionHandler(AppointmentMismatchException.class)
    public ProblemDetail handleAppointmentMismatch(AppointmentMismatchException ex) {
        log.warn("Appointment mismatch: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Appointment Mismatch", "appointment-mismatch", ex.getMessage());
    }

    @ExceptionHandler(PrescriptionNotFoundException.class)
    public ProblemDetail handlePrescriptionNotFound(PrescriptionNotFoundException ex) {
        log.warn("Prescription not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Prescription Not Found", "prescription-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.lab.exception.LabTestNotFoundException.class)
    public ProblemDetail handleLabTestNotFound(com.medcore.hms.lab.exception.LabTestNotFoundException ex) {
        log.warn("Lab test not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Lab Test Not Found", "lab-test-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.lab.exception.LabReportNotFoundException.class)
    public ProblemDetail handleLabReportNotFound(com.medcore.hms.lab.exception.LabReportNotFoundException ex) {
        log.warn("Lab report not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Lab Report Not Found", "lab-report-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.lab.exception.InvalidLabTestStatusException.class)
    public ProblemDetail handleInvalidLabTestStatus(com.medcore.hms.lab.exception.InvalidLabTestStatusException ex) {
        log.warn("Invalid lab test status: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Lab Test Status", "invalid-lab-test-status", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.pharmacy.exception.MedicineNotFoundException.class)
    public ProblemDetail handleMedicineNotFound(com.medcore.hms.pharmacy.exception.MedicineNotFoundException ex) {
        log.warn("Medicine not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Medicine Not Found", "medicine-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.pharmacy.exception.SupplierNotFoundException.class)
    public ProblemDetail handleSupplierNotFound(com.medcore.hms.pharmacy.exception.SupplierNotFoundException ex) {
        log.warn("Supplier not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Supplier Not Found", "supplier-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.pharmacy.exception.InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(com.medcore.hms.pharmacy.exception.InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Insufficient Stock", "insufficient-stock", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.pharmacy.exception.MedicineExpiredException.class)
    public ProblemDetail handleMedicineExpired(com.medcore.hms.pharmacy.exception.MedicineExpiredException ex) {
        log.warn("Medicine expired: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Medicine Expired", "medicine-expired", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.billing.exception.InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(com.medcore.hms.billing.exception.InvoiceNotFoundException ex) {
        log.warn("Invoice not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Invoice Not Found", "invoice-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.billing.exception.PaymentNotFoundException.class)
    public ProblemDetail handlePaymentNotFound(com.medcore.hms.billing.exception.PaymentNotFoundException ex) {
        log.warn("Payment not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Payment Not Found", "payment-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.billing.exception.OverpaymentException.class)
    public ProblemDetail handleOverpayment(com.medcore.hms.billing.exception.OverpaymentException ex) {
        log.warn("Overpayment error: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Overpayment Error", "overpayment-error", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.billing.exception.InvalidRefundException.class)
    public ProblemDetail handleInvalidRefund(com.medcore.hms.billing.exception.InvalidRefundException ex) {
        log.warn("Invalid refund error: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Refund Error", "invalid-refund-error", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.ipd.exception.AdmissionNotFoundException.class)
    public ProblemDetail handleAdmissionNotFound(com.medcore.hms.ipd.exception.AdmissionNotFoundException ex) {
        log.warn("Admission not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Admission Not Found", "admission-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.ipd.exception.BedNotAvailableException.class)
    public ProblemDetail handleBedNotAvailable(com.medcore.hms.ipd.exception.BedNotAvailableException ex) {
        log.warn("Bed not available: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Bed Not Available", "bed-not-available", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.ipd.exception.ActiveAdmissionExistsException.class)
    public ProblemDetail handleActiveAdmissionExists(com.medcore.hms.ipd.exception.ActiveAdmissionExistsException ex) {
        log.warn("Active admission exists: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Active Admission Exists", "active-admission-exists", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.telemedicine.exception.TelemedicineSessionNotFoundException.class)
    public ProblemDetail handleTelemedicineSessionNotFound(com.medcore.hms.telemedicine.exception.TelemedicineSessionNotFoundException ex) {
        log.warn("Telemedicine session not found: {}", ex.getMessage());
        return problem(HttpStatus.NOT_FOUND, "Telemedicine Session Not Found", "telemedicine-session-not-found", ex.getMessage());
    }

    @ExceptionHandler(com.medcore.hms.telemedicine.exception.InvalidSessionStateException.class)
    public ProblemDetail handleInvalidSessionState(com.medcore.hms.telemedicine.exception.InvalidSessionStateException ex) {
        log.warn("Invalid session state: {}", ex.getMessage());
        return problem(HttpStatus.BAD_REQUEST, "Invalid Session State", "invalid-session-state", ex.getMessage());
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", "bad-request", ex.getMessage());
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
