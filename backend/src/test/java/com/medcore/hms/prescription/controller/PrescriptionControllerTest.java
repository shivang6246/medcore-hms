package com.medcore.hms.prescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequestDto;
import com.medcore.hms.prescription.dto.PrescriptionResponseDto;
import com.medcore.hms.prescription.dto.PrescriptionSummaryDto;
import com.medcore.hms.prescription.dto.UpdatePrescriptionRequestDto;
import com.medcore.hms.prescription.exception.PrescriptionNotFoundException;
import com.medcore.hms.prescription.service.PrescriptionService;
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
@DisplayName("PrescriptionController Integration Tests")
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID prescriptionId;
    private UUID medicalRecordId;

    private CreatePrescriptionRequestDto createDto;
    private PrescriptionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        prescriptionId = UUID.randomUUID();
        medicalRecordId = UUID.randomUUID();

        createDto = new CreatePrescriptionRequestDto(
                medicalRecordId, "Ciprofloxacin", "500mg", "Twice daily", 7, "With full glass of water", 14
        );

        responseDto = new PrescriptionResponseDto(
                prescriptionId, medicalRecordId, "Ciprofloxacin", "500mg", "Twice daily", 7,
                "With full glass of water", 14, true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/prescriptions")
    class CreatePrescriptionApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("201 Created — Doctor creates prescription")
        void createPrescription_Success() throws Exception {
            when(prescriptionService.createPrescription(any(CreatePrescriptionRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/prescriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(prescriptionId.toString()))
                    .andExpect(jsonPath("$.data.medicineName").value("Ciprofloxacin"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot create prescription")
        void createPrescription_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/prescriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/prescriptions/{id}")
    class GetPrescriptionApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Fetch prescription by ID")
        void getPrescriptionById_Success() throws Exception {
            when(prescriptionService.getPrescriptionById(prescriptionId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/prescriptions/{id}", prescriptionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(prescriptionId.toString()));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — Prescription missing")
        void getPrescriptionById_NotFound() throws Exception {
            when(prescriptionService.getPrescriptionById(prescriptionId))
                    .thenThrow(new PrescriptionNotFoundException(prescriptionId));

            mockMvc.perform(get("/api/prescriptions/{id}", prescriptionId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/medical-records/{id}/prescriptions")
    class GetByMedicalRecordApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Fetch prescriptions by Medical Record ID")
        void getPrescriptionsByMedicalRecord_Success() throws Exception {
            PrescriptionSummaryDto summaryDto = new PrescriptionSummaryDto(
                    prescriptionId, "Ciprofloxacin", "500mg", "Twice daily", 7, true
            );

            PagedResponse<PrescriptionSummaryDto> pagedResponse = PagedResponse.from(
                    new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1)
            );

            when(prescriptionService.getPrescriptionsByMedicalRecord(eq(medicalRecordId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/medical-records/{id}/prescriptions", medicalRecordId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].medicineName").value("Ciprofloxacin"));
        }
    }

    @Nested
    @DisplayName("PUT & PATCH Prescription")
    class UpdateAndDeactivateApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Update prescription")
        void updatePrescription_Success() throws Exception {
            UpdatePrescriptionRequestDto updateDto = new UpdatePrescriptionRequestDto(
                    "Ciprofloxacin 250mg", "250mg", "Once daily", 5, "Take with meal", 5
            );

            when(prescriptionService.updatePrescription(eq(prescriptionId), any(UpdatePrescriptionRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/api/prescriptions/{id}", prescriptionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 OK — Deactivate prescription")
        void deactivatePrescription_Success() throws Exception {
            when(prescriptionService.deactivatePrescription(prescriptionId)).thenReturn(responseDto);

            mockMvc.perform(patch("/api/prescriptions/{id}/deactivate", prescriptionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
