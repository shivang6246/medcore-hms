package com.medcore.hms.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.dashboard.dto.AdminDashboardDto;
import com.medcore.hms.dashboard.dto.DoctorDashboardDto;
import com.medcore.hms.dashboard.dto.ReceptionDashboardDto;
import com.medcore.hms.dashboard.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DashboardController Integration Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID hospitalId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/dashboard/admin")
    class AdminDashboardApi {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — Admin fetches dashboard metrics")
        void getAdminDashboard_Success() throws Exception {
            AdminDashboardDto dto = new AdminDashboardDto(
                    100, 15, 10, new BigDecimal("15000.00"), new BigDecimal("2000.00"),
                    60.0, 50, 30, List.of(), List.of(), List.of()
            );

            when(dashboardService.getAdminDashboard(any())).thenReturn(dto);

            mockMvc.perform(get("/api/dashboard/admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalPatients").value(100))
                    .andExpect(jsonPath("$.data.monthlyRevenue").value(15000.00));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot access Admin dashboard")
        void getAdminDashboard_ForbiddenForPatient() throws Exception {
            mockMvc.perform(get("/api/dashboard/admin"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/dashboard/doctor & /reception")
    class DoctorAndReceptionApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Doctor fetches clinical dashboard")
        void getDoctorDashboard_Success() throws Exception {
            DoctorDashboardDto dto = new DoctorDashboardDto(
                    8, 3, 5, 45, List.of(), List.of(), List.of()
            );

            when(dashboardService.getDoctorDashboard(doctorId)).thenReturn(dto);

            mockMvc.perform(get("/api/dashboard/doctor").param("doctorId", doctorId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.todayTotalAppointments").value(8));
        }

        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("200 OK — Receptionist fetches operational dashboard")
        void getReceptionDashboard_Success() throws Exception {
            ReceptionDashboardDto dto = new ReceptionDashboardDto(
                    20, 12, 8, 200, 15, 35, 5, List.of()
            );

            when(dashboardService.getReceptionDashboard(any())).thenReturn(dto);

            mockMvc.perform(get("/api/dashboard/reception"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.availableBedsCount").value(15));
        }
    }
}
