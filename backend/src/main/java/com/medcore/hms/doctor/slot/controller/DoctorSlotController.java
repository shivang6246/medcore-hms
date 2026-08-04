package com.medcore.hms.doctor.slot.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.doctor.slot.dto.BlockSlotRequestDto;
import com.medcore.hms.doctor.slot.dto.GenerateSlotsRequestDto;
import com.medcore.hms.doctor.slot.dto.SlotResponseDto;
import com.medcore.hms.doctor.slot.dto.SlotSummaryDto;
import com.medcore.hms.doctor.slot.service.DoctorSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Tag(name = "Doctor Slots", description = "REST APIs for generating, querying, blocking and unblocking doctor appointment slots.")
@Slf4j
@RestController
@RequiredArgsConstructor
public class DoctorSlotController {

    private final DoctorSlotService slotService;

    @Operation(
            summary = "Generate appointment slots",
            description = "Generates AVAILABLE slots for a doctor over a date range based on active schedules. " +
                    "Skips lunch breaks. Idempotent — existing slots for a time are not duplicated. Max 90-day range."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Slots generated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Range exceeds 90 days or toDate before fromDate")
    })
    @PostMapping("/api/doctors/{doctorId}/slots/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<List<SlotSummaryDto>>> generateSlots(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Valid @RequestBody GenerateSlotsRequestDto dto) {
        log.info("Generating slots for doctor {} from {} to {}", doctorId, dto.fromDate(), dto.toDate());
        List<SlotSummaryDto> slots = slotService.generateSlots(doctorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(slots, slots.size() + " slots generated successfully"));
    }

    @Operation(
            summary = "Get slots for a doctor on a specific date",
            description = "Returns all slots (AVAILABLE, BOOKED, BLOCKED) for a doctor on the given date."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Slots returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @GetMapping("/api/doctors/{doctorId}/slots")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<SlotSummaryDto>>> getSlotsByDate(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,

            @Parameter(description = "Specific date (YYYY-MM-DD). Required if 'from' and 'to' are not provided.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Start of date range (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "End of date range (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (from != null && to != null) {
            log.debug("Fetching slots for doctor {} from {} to {}", doctorId, from, to);
            return ResponseEntity.ok(ApiResponse.success(
                    slotService.getSlotsByDoctorAndDateRange(doctorId, from, to), "Slots fetched successfully"));
        }

        LocalDate queryDate = date != null ? date : LocalDate.now();
        log.debug("Fetching slots for doctor {} on {}", doctorId, queryDate);
        return ResponseEntity.ok(ApiResponse.success(
                slotService.getSlotsByDoctorAndDate(doctorId, queryDate), "Slots fetched successfully"));
    }

    @Operation(summary = "Get a slot by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Slot returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @GetMapping("/api/slots/{slotId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<ApiResponse<SlotResponseDto>> getSlotById(
            @Parameter(description = "Slot UUID", required = true) @PathVariable UUID slotId) {
        log.debug("Fetching slot {}", slotId);
        return ResponseEntity.ok(ApiResponse.success(slotService.getSlotById(slotId), "Slot fetched successfully"));
    }

    @Operation(
            summary = "Block a slot",
            description = "Sets a slot status to BLOCKED. BOOKED slots cannot be blocked. Optional reason can be provided."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Slot blocked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Slot not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slot is already booked")
    })
    @PatchMapping("/api/slots/{slotId}/block")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<SlotResponseDto>> blockSlot(
            @Parameter(description = "Slot UUID", required = true) @PathVariable UUID slotId,
            @RequestBody(required = false) BlockSlotRequestDto dto) {
        log.info("Blocking slot {}", slotId);
        return ResponseEntity.ok(ApiResponse.success(slotService.blockSlot(slotId, dto), "Slot blocked successfully"));
    }

    @Operation(
            summary = "Unblock a slot",
            description = "Reverts a BLOCKED slot back to AVAILABLE. BOOKED slots cannot be unblocked."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Slot unblocked"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Slot not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slot is already booked")
    })
    @PatchMapping("/api/slots/{slotId}/unblock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
    public ResponseEntity<ApiResponse<SlotResponseDto>> unblockSlot(
            @Parameter(description = "Slot UUID", required = true) @PathVariable UUID slotId) {
        log.info("Unblocking slot {}", slotId);
        return ResponseEntity.ok(ApiResponse.success(slotService.unblockSlot(slotId), "Slot unblocked successfully"));
    }

    @Operation(
            summary = "Delete available slots for a doctor on a date",
            description = "Deletes all AVAILABLE (not BOOKED or BLOCKED) slots for a doctor on the given date. " +
                    "Used for regeneration after schedule changes."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Slots deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Doctor not found")
    })
    @DeleteMapping("/api/doctors/{doctorId}/slots")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAvailableSlots(
            @Parameter(description = "Doctor UUID", required = true) @PathVariable UUID doctorId,
            @Parameter(description = "Date to clear slots for", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Deleting available slots for doctor {} on {}", doctorId, date);
        int deleted = slotService.deleteAvailableSlotsByDoctorAndDate(doctorId, date);
        return ResponseEntity.ok(ApiResponse.success(null, deleted + " available slots deleted"));
    }
}
