package com.medcore.hms.dashboard.dto;

import com.medcore.hms.ipd.dto.BedResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Receptionist Dashboard statistics response DTO")
public record ReceptionDashboardDto(
        @Schema(description = "Today's total appointments count")
        long todayAppointmentsCount,

        @Schema(description = "Today's checked-in / confirmed count")
        long todayCheckedInCount,

        @Schema(description = "Today's pending check-in count")
        long todayPendingCount,

        @Schema(description = "Total registered patients count")
        long totalRegisteredPatients,

        @Schema(description = "Available IPD beds count")
        long availableBedsCount,

        @Schema(description = "Occupied IPD beds count")
        long occupiedBedsCount,

        @Schema(description = "Open/Unpaid invoices count")
        long openInvoicesCount,

        @Schema(description = "Sample list of available beds for quick allocation")
        List<BedResponseDto> availableBeds
) {}
