package com.medcore.hms.dashboard.service;

import com.medcore.hms.dashboard.dto.*;

import java.util.List;
import java.util.UUID;

public interface DashboardService {

    AdminDashboardDto getAdminDashboard(UUID hospitalId);

    DoctorDashboardDto getDoctorDashboard(UUID doctorId);

    ReceptionDashboardDto getReceptionDashboard(UUID hospitalId);

    List<AnalyticsTrendDto> getRevenueAnalytics(UUID hospitalId, int months);

    List<AnalyticsTrendDto> getAppointmentAnalytics(UUID hospitalId, int days);
}
