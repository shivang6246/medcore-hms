package com.medcore.hms.dashboard.dto;

import com.medcore.hms.appointment.dto.AppointmentResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Doctor Dashboard statistics response DTO")
public record DoctorDashboardDto(
        @Schema(description = "Today's total appointments count")
        long todayTotalAppointments,

        @Schema(description = "Today's pending/scheduled appointments count")
        long todayPendingAppointments,

        @Schema(description = "Today's completed appointments count")
        long todayCompletedAppointments,

        @Schema(description = "Total unique patients treated by doctor")
        long totalUniquePatients,

        @Schema(description = "Today's appointment schedule")
        List<AppointmentResponseDto> todaySchedule,

        @Schema(description = "Recent patient medical records created")
        List<MedicalRecordSummaryDto> recentMedicalRecords,

        @Schema(description = "Weekly appointment volume trend")
        List<AnalyticsTrendDto> weeklyAppointmentTrend
) {}
