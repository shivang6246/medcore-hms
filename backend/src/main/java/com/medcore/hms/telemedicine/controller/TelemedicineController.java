package com.medcore.hms.telemedicine.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.telemedicine.dto.*;
import com.medcore.hms.telemedicine.service.TelemedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Telemedicine & Virtual Consultations", description = "REST APIs for WebRTC video consultation rooms, virtual waiting rooms, meeting tokens, and online session history.")
@Slf4j
@RestController
@RequestMapping("/api/telemedicine")
@RequiredArgsConstructor
public class TelemedicineController {

    private final TelemedicineService telemedicineService;

    @Operation(summary = "Create video consultation room session")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Telemedicine room created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PostMapping("/sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<TelemedicineSessionResponseDto>> createSession(
            @Valid @RequestBody CreateTelemedicineSessionRequestDto dto) {
        log.info("REST request to create telemedicine room for appointment ID: {}", dto.appointmentId());
        TelemedicineSessionResponseDto response = telemedicineService.createSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Telemedicine session created successfully"));
    }

    @Operation(summary = "Get telemedicine session details")
    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<TelemedicineSessionResponseDto>> getSessionById(
            @Parameter(description = "Session UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to fetch telemedicine session ID: {}", id);
        TelemedicineSessionResponseDto response = telemedicineService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Telemedicine session details fetched successfully"));
    }

    @Operation(summary = "Join virtual waiting room / get room token")
    @PostMapping("/sessions/{id}/join")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<JoinSessionResponseDto>> joinSession(
            @Parameter(description = "Session UUID", required = true) @PathVariable UUID id,
            @RequestParam(defaultValue = "PATIENT") String role) {
        log.info("REST request to join room session ID: {} as role: {}", id, role);
        JoinSessionResponseDto response = telemedicineService.joinWaitingRoom(id, role);
        return ResponseEntity.ok(ApiResponse.success(response, "Joined virtual room successfully"));
    }

    @Operation(summary = "Start video consultation (Doctor admits patient)")
    @PostMapping("/sessions/{id}/start")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<TelemedicineSessionResponseDto>> startConsultation(
            @Parameter(description = "Session UUID", required = true) @PathVariable UUID id) {
        log.info("REST request to start video consultation for session ID: {}", id);
        TelemedicineSessionResponseDto response = telemedicineService.startConsultation(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Video consultation started"));
    }

    @Operation(summary = "Complete video consultation")
    @PostMapping("/sessions/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<TelemedicineSessionResponseDto>> completeConsultation(
            @Parameter(description = "Session UUID", required = true) @PathVariable UUID id,
            @RequestParam(required = false) String notes) {
        log.info("REST request to complete consultation for session ID: {}", id);
        TelemedicineSessionResponseDto response = telemedicineService.completeConsultation(id, notes);
        return ResponseEntity.ok(ApiResponse.success(response, "Video consultation completed successfully"));
    }

    @Operation(summary = "Get Doctor's virtual waiting room queue")
    @GetMapping("/doctor/{id}/waiting-room")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<List<TelemedicineSessionSummaryDto>>> getDoctorWaitingRoomQueue(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable("id") UUID doctorId) {
        log.info("REST request to fetch virtual waiting room queue for doctor ID: {}", doctorId);
        List<TelemedicineSessionSummaryDto> response = telemedicineService.getDoctorWaitingRoomQueue(doctorId);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor waiting room queue fetched successfully"));
    }

    @Operation(summary = "Get Patient's telemedicine consultation history")
    @GetMapping("/patient/{id}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<PagedResponse<TelemedicineSessionSummaryDto>>> getPatientConsultationHistory(
            @Parameter(description = "Patient UUID", required = true) @PathVariable("id") UUID patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("REST request to fetch consultation history for patient ID: {}", patientId);
        PagedResponse<TelemedicineSessionSummaryDto> response = telemedicineService.getPatientConsultationHistory(
                patientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scheduledStartTime")));
        return ResponseEntity.ok(ApiResponse.success(response, "Patient consultation history fetched successfully"));
    }
}
