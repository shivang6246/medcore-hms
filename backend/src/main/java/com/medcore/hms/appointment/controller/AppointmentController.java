package com.medcore.hms.appointment.controller;

import com.medcore.hms.appointment.dto.*;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.entity.AppointmentType;
import com.medcore.hms.appointment.service.AppointmentService;
import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Tag(name = "Appointment Management", description = "REST APIs for booking, rescheduling, cancelling, and managing the full appointment lifecycle.")
@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // ── Book ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Book an appointment",
            description = "Books an AVAILABLE slot for a patient with a doctor. Slot status becomes BOOKED atomically. " +
                    "Validates patient/doctor belong to same hospital.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Appointment booked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient, doctor or slot not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slot not available or hospital mismatch")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> bookAppointment(
            @Valid @RequestBody BookAppointmentRequestDto dto) {
        log.info("Book appointment request — patient: {}, slot: {}", dto.patientId(), dto.slotId());
        AppointmentResponseDto result = appointmentService.bookAppointment(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Appointment booked successfully"));
    }

    // ── Get ───────────────────────────────────────────────────────────────────

    @Operation(summary = "Get appointment by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> getAppointmentById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAppointmentById(id), "Appointment fetched successfully"));
    }

    // ── Lists ─────────────────────────────────────────────────────────────────

    @Operation(summary = "List appointments by hospital (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentSummaryDto>>> getByHospital(
            @Parameter(description = "Hospital UUID", required = true) @RequestParam UUID hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAppointmentsByHospital(
                        hospitalId, PageRequest.of(page, size, parseSort(sort))),
                "Appointments fetched successfully"));
    }

    @Operation(summary = "List appointments by doctor (paginated)")
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentSummaryDto>>> getByDoctor(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAppointmentsByDoctor(
                        doctorId, PageRequest.of(page, size, parseSort(sort))),
                "Doctor appointments fetched successfully"));
    }

    @Operation(summary = "List appointments by patient (paginated)")
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentSummaryDto>>> getByPatient(
            @PathVariable UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getAppointmentsByPatient(
                        patientId, PageRequest.of(page, size, parseSort(sort))),
                "Patient appointments fetched successfully"));
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Operation(summary = "Search appointments",
            description = "Multi-criteria search by hospital, patient, doctor, status, type and date range. All parameters optional.")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PagedResponse<AppointmentSummaryDto>>> searchAppointments(
            @RequestParam(required = false) UUID hospitalId,
            @RequestParam(required = false) UUID patientId,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) AppointmentType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {
        AppointmentSearchCriteria criteria = new AppointmentSearchCriteria(
                hospitalId, patientId, doctorId, status, type, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.searchAppointments(criteria, PageRequest.of(page, size, parseSort(sort))),
                "Search completed successfully"));
    }

    // ── Daily Schedule ────────────────────────────────────────────────────────

    @Operation(summary = "Get doctor's daily schedule",
            description = "Returns all slots for a doctor on a date with appointment details for booked ones. " +
                    "Includes slot counts (total/booked/available).")
    @GetMapping("/doctor/{doctorId}/schedule")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DailyScheduleDto>> getDailySchedule(
            @PathVariable UUID doctorId,
            @Parameter(description = "Date (YYYY-MM-DD), defaults to today")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.getDoctorDailySchedule(doctorId, queryDate),
                "Daily schedule fetched successfully"));
    }

    // ── Workflow Actions ──────────────────────────────────────────────────────

    @Operation(summary = "Confirm appointment", description = "SCHEDULED → CONFIRMED")
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.confirmAppointment(id), "Appointment confirmed"));
    }

    @Operation(summary = "Check-in patient", description = "SCHEDULED/CONFIRMED → CHECKED_IN")
    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'NURSE', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> checkIn(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.checkInPatient(id), "Patient checked in"));
    }

    @Operation(summary = "Mark appointment as in-progress", description = "CHECKED_IN → IN_PROGRESS")
    @PatchMapping("/{id}/in-progress")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> inProgress(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.markInProgress(id), "Appointment in progress"));
    }

    @Operation(summary = "Complete appointment", description = "IN_PROGRESS/CHECKED_IN → COMPLETED")
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.completeAppointment(id), "Appointment completed"));
    }

    @Operation(summary = "Cancel appointment", description = "Releases the slot back to AVAILABLE.")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelAppointmentRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.cancelAppointment(id, dto), "Appointment cancelled"));
    }

    @Operation(summary = "Mark patient as no-show", description = "Releases the slot and sets status to NO_SHOW.")
    @PatchMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> noShow(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.markNoShow(id), "Appointment marked as no-show"));
    }

    @Operation(summary = "Reschedule appointment",
            description = "Atomically releases old slot and books new slot. New slot must be AVAILABLE and belong to same doctor.")
    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> reschedule(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.rescheduleAppointment(id, dto), "Appointment rescheduled"));
    }

    @Operation(summary = "Update appointment notes/chief complaint", description = "Accessible by the treating doctor.")
    @PatchMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateNotes(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentNotesRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                appointmentService.updateNotes(id, dto), "Notes updated"));
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return Sort.by(Sort.Direction.DESC, "appointmentDate");
        String[] parts = sortParam.split(",");
        Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, parts[0].trim());
    }
}
