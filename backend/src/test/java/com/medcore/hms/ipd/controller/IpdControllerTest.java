package com.medcore.hms.ipd.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.AdmissionStatus;
import com.medcore.hms.ipd.entity.BedStatus;
import com.medcore.hms.ipd.exception.AdmissionNotFoundException;
import com.medcore.hms.ipd.service.IpdService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("IpdController Integration Tests")
class IpdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IpdService ipdService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID admissionId;
    private UUID patientId;
    private UUID doctorId;
    private UUID wardId;
    private UUID roomId;
    private UUID bedId;

    private CreateAdmissionRequestDto createDto;
    private AdmissionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        admissionId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        wardId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        bedId = UUID.randomUUID();

        createDto = new CreateAdmissionRequestDto(
                patientId, doctorId, wardId, roomId, bedId, LocalDateTime.now(), null, "Fever"
        );

        responseDto = new AdmissionResponseDto(
                admissionId, "ADM-2026-01", patientId, "Bruce Wayne", doctorId, "Dr. House",
                "General Ward", "101", "B1", LocalDateTime.now(), null, null, "Fever",
                AdmissionStatus.ADMITTED, null
        );
    }

    @Nested
    @DisplayName("POST /api/admissions")
    class CreateAdmissionApi {

        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("201 Created — Receptionist admits patient")
        void createAdmission_Success() throws Exception {
            when(ipdService.createAdmission(any(CreateAdmissionRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/admissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(admissionId.toString()))
                    .andExpect(jsonPath("$.data.admissionNumber").value("ADM-2026-01"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot create admission")
        void createAdmission_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/admissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/admissions/{id}")
    class GetAdmissionApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Fetch admission by ID")
        void getAdmissionById_Success() throws Exception {
            when(ipdService.getAdmissionById(admissionId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/admissions/{id}", admissionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(admissionId.toString()));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — Admission missing")
        void getAdmissionById_NotFound() throws Exception {
            when(ipdService.getAdmissionById(admissionId))
                    .thenThrow(new AdmissionNotFoundException(admissionId));

            mockMvc.perform(get("/api/admissions/{id}", admissionId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/admissions/{id}/discharge & GET /api/beds/available")
    class DischargeAndBedsApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Discharge patient")
        void dischargePatient_Success() throws Exception {
            DischargeRequestDto dischargeDto = new DischargeRequestDto(
                    LocalDateTime.now(), "Discharged", "Summary", "Notes", "Follow up", null
            );

            when(ipdService.dischargePatient(eq(admissionId), any(DischargeRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(patch("/api/admissions/{id}/discharge", admissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dischargeDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "RECEPTIONIST")
        @DisplayName("200 OK — Fetch available beds")
        void getAvailableBeds_Success() throws Exception {
            BedResponseDto bedResponse = new BedResponseDto(
                    bedId, roomId, "101", "General Ward", "B1", BedStatus.AVAILABLE, new BigDecimal("100.00"), true
            );

            PagedResponse<BedResponseDto> pagedResponse = PagedResponse.from(
                    new PageImpl<>(List.of(bedResponse), PageRequest.of(0, 10), 1)
            );

            when(ipdService.getAvailableBeds(any(), any(Pageable.class))).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/beds/available"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].bedNumber").value("B1"));
        }
    }
}
