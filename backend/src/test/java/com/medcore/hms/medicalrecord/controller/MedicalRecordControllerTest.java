package com.medcore.hms.medicalrecord.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.exception.DuplicateMedicalRecordException;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.medicalrecord.service.MedicalRecordService;
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

import java.time.LocalDate;
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
@DisplayName("MedicalRecordController Integration Tests")
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicalRecordService medicalRecordService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID recordId;
    private UUID patientId;
    private UUID doctorId;
    private UUID appointmentId;

    private CreateMedicalRecordRequestDto createDto;
    private MedicalRecordResponseDto responseDto;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        createDto = new CreateMedicalRecordRequestDto(
                patientId, doctorId, appointmentId,
                "Fever, Cough", "Upper Respiratory Infection",
                "Azithromycin and rest", "Drink fluids",
                LocalDate.now().plusDays(7)
        );

        responseDto = new MedicalRecordResponseDto(
                recordId,
                new MedicalRecordResponseDto.PatientRefDto(patientId, "P-100", "John", "Doe", "1234567890"),
                new MedicalRecordResponseDto.DoctorRefDto(doctorId, "Alice", "Smith", "General"),
                new MedicalRecordResponseDto.AppointmentRefDto(appointmentId, "APT-101", LocalDate.now()),
                "Fever, Cough", "Upper Respiratory Infection",
                "Azithromycin and rest", "Drink fluids",
                LocalDate.now().plusDays(7),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/medical-records")
    class CreateRecordApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("201 Created — Doctor creates medical record")
        void createMedicalRecord_Success() throws Exception {
            when(medicalRecordService.createMedicalRecord(any(CreateMedicalRecordRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/medical-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(recordId.toString()))
                    .andExpect(jsonPath("$.data.symptoms").value("Fever, Cough"))
                    .andExpect(jsonPath("$.data.diagnosis").value("Upper Respiratory Infection"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot create medical record")
        void createMedicalRecord_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/medical-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("400 Bad Request — Blank symptoms validation error")
        void createMedicalRecord_ValidationError() throws Exception {
            CreateMedicalRecordRequestDto invalidDto = new CreateMedicalRecordRequestDto(
                    patientId, doctorId, appointmentId,
                    "", "Diagnosis", "Plan", "Notes", null
            );

            mockMvc.perform(post("/api/medical-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("409 Conflict — Duplicate record for appointment")
        void createMedicalRecord_DuplicateConflict() throws Exception {
            when(medicalRecordService.createMedicalRecord(any(CreateMedicalRecordRequestDto.class)))
                    .thenThrow(new DuplicateMedicalRecordException(appointmentId));

            mockMvc.perform(post("/api/medical-records")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/medical-records/{id}")
    class GetRecordApi {

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 OK — Fetch record by ID")
        void getMedicalRecordById_Success() throws Exception {
            when(medicalRecordService.getMedicalRecordById(recordId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/medical-records/{id}", recordId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(recordId.toString()));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — Record does not exist")
        void getMedicalRecordById_NotFound() throws Exception {
            when(medicalRecordService.getMedicalRecordById(recordId))
                    .thenThrow(new MedicalRecordNotFoundException(recordId));

            mockMvc.perform(get("/api/medical-records/{id}", recordId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/patients/{patientId}/medical-records")
    class GetByPatientApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Fetch medical records by patient ID")
        void getMedicalRecordsByPatient_Success() throws Exception {
            MedicalRecordSummaryDto summaryDto = new MedicalRecordSummaryDto(
                    recordId, "John Doe", "Alice Smith", "APT-101",
                    "Upper Respiratory Infection", LocalDate.now().plusDays(7), true, LocalDateTime.now()
            );

            PagedResponse<MedicalRecordSummaryDto> pagedResponse = PagedResponse.from(
                    new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1)
            );

            when(medicalRecordService.getMedicalRecordsByPatient(eq(patientId), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/patients/{patientId}/medical-records", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(recordId.toString()));
        }
    }

    @Nested
    @DisplayName("PUT & PATCH Medical Record")
    class UpdateAndDeactivateApi {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — Update medical record")
        void updateMedicalRecord_Success() throws Exception {
            UpdateMedicalRecordRequestDto updateDto = new UpdateMedicalRecordRequestDto(
                    "Resolved symptoms", "Recovered", "No medication", "Discharged", null
            );

            when(medicalRecordService.updateMedicalRecord(eq(recordId), any(UpdateMedicalRecordRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/api/medical-records/{id}", recordId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 OK — Deactivate medical record")
        void deactivateMedicalRecord_Success() throws Exception {
            when(medicalRecordService.deactivateMedicalRecord(recordId)).thenReturn(responseDto);

            mockMvc.perform(patch("/api/medical-records/{id}/deactivate", recordId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
