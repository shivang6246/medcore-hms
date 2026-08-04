package com.medcore.hms.telemedicine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.telemedicine.dto.*;
import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import com.medcore.hms.telemedicine.exception.TelemedicineSessionNotFoundException;
import com.medcore.hms.telemedicine.service.TelemedicineService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TelemedicineController Integration Tests")
class TelemedicineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TelemedicineService telemedicineService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID sessionId;
    private UUID appointmentId;
    private CreateTelemedicineSessionRequestDto createDto;
    private TelemedicineSessionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        createDto = new CreateTelemedicineSessionRequestDto(appointmentId, LocalDateTime.now());

        responseDto = new TelemedicineSessionResponseDto(
                sessionId, "ROOM-2026-01", "https://telehealth.medcore.hms/meet/ROOM-2026-01",
                appointmentId, UUID.randomUUID(), "Dr. Strange", UUID.randomUUID(), "Peter Parker",
                LocalDateTime.now(), null, null, ConsultationSessionStatus.WAITING_ROOM, null, LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/telemedicine/sessions")
    class CreateSessionApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("201 Created — Doctor creates video room session")
        void createSession_Success() throws Exception {
            when(telemedicineService.createSession(any(CreateTelemedicineSessionRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/telemedicine/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(sessionId.toString()))
                    .andExpect(jsonPath("$.data.roomCode").value("ROOM-2026-01"));
        }
    }

    @Nested
    @DisplayName("GET /api/telemedicine/sessions/{id} & JOIN / START / COMPLETE")
    class SessionWorkflowApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Patient joins virtual waiting room")
        void joinSession_PatientSuccess() throws Exception {
            JoinSessionResponseDto joinDto = new JoinSessionResponseDto(
                    sessionId, "ROOM-2026-01", "https://telehealth.medcore.hms/meet/ROOM-2026-01", "PATIENT", "PAT-TOK-1"
            );

            when(telemedicineService.joinWaitingRoom(eq(sessionId), any())).thenReturn(joinDto);

            mockMvc.perform(post("/api/telemedicine/sessions/{id}/join", sessionId).param("role", "PATIENT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("PATIENT"));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Doctor starts video consultation")
        void startConsultation_Success() throws Exception {
            when(telemedicineService.startConsultation(sessionId)).thenReturn(responseDto);

            mockMvc.perform(post("/api/telemedicine/sessions/{id}/start", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — Session missing")
        void getSessionById_NotFound() throws Exception {
            when(telemedicineService.getSessionById(sessionId))
                    .thenThrow(new TelemedicineSessionNotFoundException(sessionId));

            mockMvc.perform(get("/api/telemedicine/sessions/{id}", sessionId))
                    .andExpect(status().isNotFound());
        }
    }
}
