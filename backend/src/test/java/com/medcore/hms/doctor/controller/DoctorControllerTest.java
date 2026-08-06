package com.medcore.hms.doctor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.dto.*;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.doctor.exception.*;
import com.medcore.hms.department.exception.DepartmentNotFoundException;
import com.medcore.hms.doctor.service.DoctorService;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("DoctorController — Integration Tests")
class DoctorControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private DoctorService doctorService;
    @MockBean private JwtService jwtService;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private UUID doctorId;
    private UUID departmentId;
    private CreateDoctorRequestDto createDto;
    private DoctorResponseDto responseDto;
    private DoctorSummaryDto summaryDto;

    @BeforeEach
    void setUp() {
        doctorId     = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();

        createDto = new CreateDoctorRequestDto(
                "Arjun", "Sharma",
                "dr.arjun@test.com", "Password123!",
                "+91-9999999999", "EMP-001",
                Gender.MALE, LocalDate.of(1980, 1, 1),
                hospitalId, departmentId,
                "LIC-DOC-001", "Cardiology", "MD",
                10, new BigDecimal("500.00"),
                null, null
        );

        responseDto = new DoctorResponseDto(
                doctorId, UUID.randomUUID(), "EMP-001",
                "Arjun", "Sharma", "dr.arjun@test.com",
                "+91-9999999999", Gender.MALE, LocalDate.of(1980, 1, 1),
                "LIC-DOC-001", "Cardiology", "MD",
                10, new BigDecimal("500.00"),
                null, null, true, true,
                new HospitalRefDto(hospitalId, "Test Hospital"),
                new DepartmentRefDto(departmentId, "Cardiology"),
                LocalDateTime.now(), LocalDateTime.now()
        );

        summaryDto = new DoctorSummaryDto(
                doctorId, "EMP-001", "Arjun Sharma",
                "dr.arjun@test.com", "Cardiology", "Cardiology",
                "Test Hospital", new BigDecimal("500.00"), true
        );
    }

    // =========================================================================
    // POST /api/doctors
    // =========================================================================

    @Nested
    @DisplayName("POST /api/doctors — Create Doctor")
    class CreateDoctorTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("201 — SUPER_ADMIN creates doctor successfully")
        void create_ShouldReturn201_WhenSuperAdmin() throws Exception {
            when(doctorService.createDoctor(any())).thenReturn(responseDto);

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("dr.arjun@test.com"))
                    .andExpect(jsonPath("$.data.licenseNumber").value("LIC-DOC-001"));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("201 — HOSPITAL_ADMIN creates doctor successfully")
        void create_ShouldReturn201_WhenHospitalAdmin() throws Exception {
            when(doctorService.createDoctor(any())).thenReturn(responseDto);

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.employeeId").value("EMP-001"));
        }

        @Test
        @DisplayName("401 — unauthenticated request is rejected")
        void create_ShouldReturn401_WhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 — PATIENT cannot create doctor")
        void create_ShouldReturn403_WhenPatient() throws Exception {
            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("403 — DOCTOR cannot create another doctor")
        void create_ShouldReturn403_WhenDoctor() throws Exception {
            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("400 — missing required fields returns validation error")
        void create_ShouldReturn400_WhenMissingRequiredFields() throws Exception {
            String badBody = "{\"email\": \"not-an-email\"}";
            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("409 — duplicate email returns conflict")
        void create_ShouldReturn409_WhenDuplicateEmail() throws Exception {
            when(doctorService.createDoctor(any()))
                    .thenThrow(new DuplicateDoctorEmailException("dr.arjun@test.com"));

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("409 — duplicate license number returns conflict")
        void create_ShouldReturn409_WhenDuplicateLicense() throws Exception {
            when(doctorService.createDoctor(any()))
                    .thenThrow(new DuplicateLicenseNumberException("LIC-DOC-001"));

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 — hospital not found returns not found")
        void create_ShouldReturn404_WhenHospitalNotFound() throws Exception {
            when(doctorService.createDoctor(any()))
                    .thenThrow(new HospitalNotFoundException(UUID.randomUUID()));

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 — department not found returns not found")
        void create_ShouldReturn404_WhenDepartmentNotFound() throws Exception {
            when(doctorService.createDoctor(any()))
                    .thenThrow(new DepartmentNotFoundException(UUID.randomUUID()));

            mockMvc.perform(post("/api/doctors")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // GET /api/doctors
    // =========================================================================

    @Nested
    @DisplayName("GET /api/doctors — List Doctors")
    class GetAllDoctorsTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 — returns paginated doctor summaries")
        void getAll_ShouldReturn200_WithPagedResponse() throws Exception {
            PagedResponse<DoctorSummaryDto> paged = PagedResponse.from(
                    new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1));
            when(doctorService.getAllDoctors(any())).thenReturn(paged);

            mockMvc.perform(get("/api/doctors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].fullName").value("Arjun Sharma"));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 — DOCTOR can list doctors")
        void getAll_ShouldReturn200_WhenDoctor() throws Exception {
            PagedResponse<DoctorSummaryDto> paged = PagedResponse.from(
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));
            when(doctorService.getAllDoctors(any())).thenReturn(paged);

            mockMvc.perform(get("/api/doctors"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("200 — PATIENT can list doctors for booking")
        void getAll_ShouldReturn200_WhenPatient() throws Exception {
            PagedResponse<DoctorSummaryDto> paged = PagedResponse.from(
                    new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1));
            when(doctorService.getAllDoctors(any())).thenReturn(paged);

            mockMvc.perform(get("/api/doctors"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("401 — unauthenticated request rejected")
        void getAll_ShouldReturn401_WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/doctors"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/doctors/{id}
    // =========================================================================

    @Nested
    @DisplayName("GET /api/doctors/{id} — Get Doctor by ID")
    class GetByIdTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 — returns full doctor profile")
        void getById_ShouldReturn200_WhenFound() throws Exception {
            when(doctorService.getDoctorById(doctorId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/doctors/{id}", doctorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value("dr.arjun@test.com"))
                    .andExpect(jsonPath("$.data.specialization").value("Cardiology"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 — invalid doctor ID returns not found")
        void getById_ShouldReturn404_WhenNotFound() throws Exception {
            when(doctorService.getDoctorById(doctorId)).thenThrow(new DoctorNotFoundException(doctorId));

            mockMvc.perform(get("/api/doctors/{id}", doctorId))
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // PUT /api/doctors/{id}
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/doctors/{id} — Update Doctor")
    class UpdateDoctorTests {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 — updates doctor profile successfully")
        void update_ShouldReturn200_WhenFound() throws Exception {
            UpdateDoctorRequestDto dto = new UpdateDoctorRequestDto(
                    null, null, null, null, null,
                    "Interventional Cardiology", null, 12, null, null, null);
            when(doctorService.updateDoctor(eq(doctorId), any())).thenReturn(responseDto);

            mockMvc.perform(put("/api/doctors/{id}", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("403 — DOCTOR cannot update another doctor's profile")
        void update_ShouldReturn403_WhenDoctor() throws Exception {
            mockMvc.perform(put("/api/doctors/{id}", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // PATCH /api/doctors/{id}/activate + deactivate
    // =========================================================================

    @Nested
    @DisplayName("PATCH /{id}/activate and /{id}/deactivate")
    class ActivationTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 — activates doctor")
        void activate_ShouldReturn200() throws Exception {
            doNothing().when(doctorService).activateDoctor(doctorId);
            mockMvc.perform(patch("/api/doctors/{id}/activate", doctorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Doctor activated successfully"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 — deactivates doctor")
        void deactivate_ShouldReturn200() throws Exception {
            doNothing().when(doctorService).deactivateDoctor(doctorId);
            mockMvc.perform(patch("/api/doctors/{id}/deactivate", doctorId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Doctor deactivated successfully"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 — activate unknown doctor returns not found")
        void activate_ShouldReturn404_WhenNotFound() throws Exception {
            doThrow(new DoctorNotFoundException(doctorId)).when(doctorService).activateDoctor(doctorId);
            mockMvc.perform(patch("/api/doctors/{id}/activate", doctorId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 — PATIENT cannot activate doctor")
        void activate_ShouldReturn403_WhenPatient() throws Exception {
            mockMvc.perform(patch("/api/doctors/{id}/activate", doctorId))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // PATCH /api/doctors/{id}/department
    // =========================================================================

    @Nested
    @DisplayName("PATCH /{id}/department — Assign Department")
    class AssignDepartmentTests {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 — assigns department successfully")
        void assign_ShouldReturn200_WhenValid() throws Exception {
            AssignDepartmentRequestDto dto = new AssignDepartmentRequestDto(departmentId);
            when(doctorService.assignDepartment(eq(doctorId), eq(departmentId))).thenReturn(responseDto);

            mockMvc.perform(patch("/api/doctors/{id}/department", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Department assigned successfully"));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("409 — department from different hospital returns conflict")
        void assign_ShouldReturn409_WhenWrongHospital() throws Exception {
            AssignDepartmentRequestDto dto = new AssignDepartmentRequestDto(departmentId);
            when(doctorService.assignDepartment(any(), any()))
                    .thenThrow(new InvalidDepartmentAssignmentException(departmentId, UUID.randomUUID()));

            mockMvc.perform(patch("/api/doctors/{id}/department", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }
    }

    // =========================================================================
    // PATCH /api/doctors/{id}/availability
    // =========================================================================

    @Nested
    @DisplayName("PATCH /{id}/availability — Update Availability")
    class AvailabilityTests {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 — doctor can update own availability")
        void availability_ShouldReturn200_WhenDoctor() throws Exception {
            UpdateAvailabilityRequestDto dto = new UpdateAvailabilityRequestDto(false);
            doNothing().when(doctorService).updateAvailability(doctorId, false);

            mockMvc.perform(patch("/api/doctors/{id}/availability", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Availability updated successfully"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 — PATIENT cannot update availability")
        void availability_ShouldReturn403_WhenPatient() throws Exception {
            mockMvc.perform(patch("/api/doctors/{id}/availability", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"available\":true}"))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // PATCH /api/doctors/{id}/consultation-fee
    // =========================================================================

    @Nested
    @DisplayName("PATCH /{id}/consultation-fee — Update Fee")
    class ConsultationFeeTests {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 — updates consultation fee successfully")
        void updateFee_ShouldReturn200_WhenValid() throws Exception {
            UpdateConsultationFeeRequestDto dto = new UpdateConsultationFeeRequestDto(new BigDecimal("750.00"));
            when(doctorService.updateConsultationFee(eq(doctorId), any())).thenReturn(responseDto);

            mockMvc.perform(patch("/api/doctors/{id}/consultation-fee", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("400 — negative fee returns validation error")
        void updateFee_ShouldReturn400_WhenNegativeFee() throws Exception {
            String body = "{\"fee\": -100.00}";
            mockMvc.perform(patch("/api/doctors/{id}/consultation-fee", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("404 — doctor not found returns not found")
        void updateFee_ShouldReturn404_WhenNotFound() throws Exception {
            UpdateConsultationFeeRequestDto dto = new UpdateConsultationFeeRequestDto(new BigDecimal("500.00"));
            when(doctorService.updateConsultationFee(any(), any()))
                    .thenThrow(new DoctorNotFoundException(doctorId));

            mockMvc.perform(patch("/api/doctors/{id}/consultation-fee", doctorId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }
}
