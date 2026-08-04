package com.medcore.hms.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.lab.dto.*;
import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.entity.TestPriority;
import com.medcore.hms.lab.exception.LabTestNotFoundException;
import com.medcore.hms.lab.service.LabTestService;
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
@DisplayName("LabTestController Integration Tests")
class LabTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabTestService labTestService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID labTestId;
    private UUID patientId;
    private UUID doctorId;

    private CreateLabTestRequestDto createDto;
    private LabTestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        labTestId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        createDto = new CreateLabTestRequestDto(
                patientId, doctorId, null, null, "Lipid Profile", TestPriority.NORMAL, "No special instructions"
        );

        responseDto = new LabTestResponseDto(
                labTestId,
                new LabTestResponseDto.PatientRefDto(patientId, "P-10", "Jane", "Doe", "1234567890"),
                new LabTestResponseDto.DoctorRefDto(doctorId, "Dr", "Smith", "Cardiology"),
                null, null, null, "Lipid Profile", TestPriority.NORMAL,
                LabTestStatus.REQUESTED, "No special instructions", true, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/lab-tests")
    class OrderLabTestApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("201 Created — Doctor orders lab test")
        void createLabTest_Success() throws Exception {
            when(labTestService.createLabTest(any(CreateLabTestRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/lab-tests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(labTestId.toString()))
                    .andExpect(jsonPath("$.data.testType").value("Lipid Profile"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot order lab test")
        void createLabTest_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/lab-tests")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/lab-tests/{id}")
    class GetLabTestApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Fetch lab test by ID")
        void getLabTestById_Success() throws Exception {
            when(labTestService.getLabTestById(labTestId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/lab-tests/{id}", labTestId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(labTestId.toString()));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — Lab test missing")
        void getLabTestById_NotFound() throws Exception {
            when(labTestService.getLabTestById(labTestId))
                    .thenThrow(new LabTestNotFoundException(labTestId));

            mockMvc.perform(get("/api/lab-tests/{id}", labTestId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/lab-tests/{id}/status & POST /api/lab-tests/{id}/report")
    class StatusAndReportApi {

        @Test
        @WithMockUser(roles = "LAB_TECHNICIAN")
        @DisplayName("200 OK — Lab technician updates status")
        void updateLabTestStatus_Success() throws Exception {
            UpdateLabTestStatusRequestDto statusDto = new UpdateLabTestStatusRequestDto(LabTestStatus.SAMPLE_COLLECTED, null);

            when(labTestService.updateLabTestStatus(eq(labTestId), any(UpdateLabTestStatusRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(patch("/api/lab-tests/{id}/status", labTestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "LAB_TECHNICIAN")
        @DisplayName("201 Created — Lab technician publishes report")
        void publishLabReport_Success() throws Exception {
            CreateLabReportRequestDto reportDto = new CreateLabReportRequestDto(
                    "Total Cholesterol: 180 mg/dL", "Normal", "http://reports/lipid.pdf", null
            );

            LabReportResponseDto reportResponse = new LabReportResponseDto(
                    UUID.randomUUID(), labTestId, "Total Cholesterol: 180 mg/dL", "Normal",
                    "http://reports/lipid.pdf", LocalDateTime.now(), "Tech Name"
            );

            when(labTestService.publishLabReport(eq(labTestId), any(CreateLabReportRequestDto.class)))
                    .thenReturn(reportResponse);

            mockMvc.perform(post("/api/lab-tests/{id}/report", labTestId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reportDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.result").value("Total Cholesterol: 180 mg/dL"));
        }
    }
}
