package com.medcore.hms.hospital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import com.medcore.hms.hospital.exception.DuplicateHospitalEmailException;
import com.medcore.hms.hospital.exception.DuplicateLicenseNumberException;
import com.medcore.hms.hospital.exception.DuplicateRegistrationNumberException;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.service.HospitalService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration-style controller tests for {@link HospitalController}.
 *
 * <p>Uses Spring Boot full context with MockMvc and Mockito stubs for:
 * <ul>
 *   <li>CRUD operations (201 Created, 200 OK, 404 Not Found)</li>
 *   <li>Validation errors (400 Bad Request)</li>
 *   <li>Duplicate conflict errors (409 Conflict)</li>
 *   <li>Authentication enforcement (401 Unauthorized)</li>
 *   <li>Authorization enforcement (403 Forbidden)</li>
 *   <li>Pagination, search, filter, sort</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("HospitalController — Integration Tests")
class HospitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HospitalService hospitalService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UUID hospitalId;
    private AddressDto addressDto;
    private CreateHospitalRequestDto createDto;
    private HospitalResponseDto responseDto;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        addressDto = new AddressDto("123 Health St", "Boston", "MA", "02108", "USA");

        createDto = new CreateHospitalRequestDto(
                "General Hospital",
                "REG-999",
                "LIC-999",
                "admin@generalhospital.com",
                "+1-555-0100",
                "https://generalhospital.com",
                "Leading hospital",
                "https://generalhospital.com/logo.png",
                addressDto
        );

        responseDto = new HospitalResponseDto(
                hospitalId,
                "General Hospital",
                "REG-999",
                "LIC-999",
                "admin@generalhospital.com",
                "+1-555-0100",
                "https://generalhospital.com",
                "Leading hospital",
                "https://generalhospital.com/logo.png",
                true,
                addressDto,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    // =========================================================================
    // POST /api/hospitals — Create
    // =========================================================================

    @Nested
    @DisplayName("POST /api/hospitals — Create Hospital")
    class CreateHospitalTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("201 Created — valid payload, SUPER_ADMIN")
        void create_ShouldReturn201_WhenSuperAdminAndValidPayload() throws Exception {
            when(hospitalService.createHospital(any(CreateHospitalRequestDto.class))).thenReturn(responseDto);

            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(hospitalId.toString()))
                    .andExpect(jsonPath("$.data.name").value("General Hospital"))
                    .andExpect(jsonPath("$.data.registrationNumber").value("REG-999"))
                    .andExpect(jsonPath("$.data.licenseNumber").value("LIC-999"))
                    .andExpect(jsonPath("$.data.email").value("admin@generalhospital.com"))
                    .andExpect(jsonPath("$.data.isActive").value(true))
                    .andExpect(jsonPath("$.data.address.city").value("Boston"));

            verify(hospitalService).createHospital(any(CreateHospitalRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("400 Bad Request — blank required fields and invalid email/phone")
        void create_ShouldReturn400_WhenValidationFails() throws Exception {
            CreateHospitalRequestDto invalid = new CreateHospitalRequestDto(
                    "", "", "", "not-an-email", "badphone", "", "", "", null
            );

            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Validation Error"))
                    .andExpect(jsonPath("$.errors").exists());

            verify(hospitalService, never()).createHospital(any());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("409 Conflict — duplicate registration number")
        void create_ShouldReturn409_WhenDuplicateRegistrationNumber() throws Exception {
            when(hospitalService.createHospital(any()))
                    .thenThrow(new DuplicateRegistrationNumberException("REG-999"));

            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Duplicate Registration Number"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("409 Conflict — duplicate license number")
        void create_ShouldReturn409_WhenDuplicateLicenseNumber() throws Exception {
            when(hospitalService.createHospital(any()))
                    .thenThrow(new DuplicateLicenseNumberException("LIC-999"));

            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Duplicate License Number"));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("409 Conflict — duplicate hospital email")
        void create_ShouldReturn409_WhenDuplicateEmail() throws Exception {
            when(hospitalService.createHospital(any()))
                    .thenThrow(new DuplicateHospitalEmailException("admin@generalhospital.com"));

            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Duplicate Hospital Email"));
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("403 Forbidden — HOSPITAL_ADMIN cannot create hospitals")
        void create_ShouldReturn403_WhenHospitalAdmin() throws Exception {
            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).createHospital(any());
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — PATIENT cannot create hospitals")
        void create_ShouldReturn403_WhenPatient() throws Exception {
            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).createHospital(any());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void create_ShouldReturn401_WhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/hospitals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/hospitals — Paginated list
    // =========================================================================

    @Nested
    @DisplayName("GET /api/hospitals — List Hospitals")
    class GetHospitalsTests {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 OK — paginated response with search/filter/sort")
        void getHospitals_ShouldReturn200_WithPaginatedData() throws Exception {
            HospitalSummaryDto summary = new HospitalSummaryDto(
                    hospitalId, "General Hospital", "REG-999", "LIC-999",
                    "admin@generalhospital.com", "+1-555-0100", null, true
            );
            PagedResponse<HospitalSummaryDto> pagedResponse = PagedResponse.from(
                    new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1)
            );

            when(hospitalService.getHospitals(eq("General"), eq(true), eq("Boston"), any(Pageable.class)))
                    .thenReturn(pagedResponse);

            mockMvc.perform(get("/api/hospitals")
                            .param("search", "General")
                            .param("isActive", "true")
                            .param("city", "Boston")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "name,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].name").value("General Hospital"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.first").value(true))
                    .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — SUPER_ADMIN can list all hospitals")
        void getHospitals_ShouldReturn200_WhenSuperAdmin() throws Exception {
            PagedResponse<HospitalSummaryDto> empty = PagedResponse.from(
                    new PageImpl<>(List.of(), PageRequest.of(0, 10), 0)
            );
            when(hospitalService.getHospitals(any(), any(), any(), any(Pageable.class))).thenReturn(empty);

            mockMvc.perform(get("/api/hospitals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("403 Forbidden — DOCTOR cannot list hospitals")
        void getHospitals_ShouldReturn403_WhenDoctor() throws Exception {
            mockMvc.perform(get("/api/hospitals"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void getHospitals_ShouldReturn401_WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/hospitals"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/hospitals/{id} — Get by ID
    // =========================================================================

    @Nested
    @DisplayName("GET /api/hospitals/{id} — Get Hospital By ID")
    class GetByIdTests {

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("200 OK — DOCTOR can read hospital details")
        void getById_ShouldReturn200_WhenDoctor() throws Exception {
            when(hospitalService.getHospitalById(hospitalId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(hospitalId.toString()))
                    .andExpect(jsonPath("$.data.name").value("General Hospital"))
                    .andExpect(jsonPath("$.data.address.city").value("Boston"));
        }

        @Test
        @WithMockUser(roles = "NURSE")
        @DisplayName("200 OK — NURSE can read hospital details")
        void getById_ShouldReturn200_WhenNurse() throws Exception {
            when(hospitalService.getHospitalById(hospitalId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DEPARTMENT_HEAD")
        @DisplayName("200 OK — DEPARTMENT_HEAD can read hospital details")
        void getById_ShouldReturn200_WhenDepartmentHead() throws Exception {
            when(hospitalService.getHospitalById(hospitalId)).thenReturn(responseDto);

            mockMvc.perform(get("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("404 Not Found — hospital does not exist")
        void getById_ShouldReturn404_WhenHospitalDoesNotExist() throws Exception {
            when(hospitalService.getHospitalById(hospitalId))
                    .thenThrow(new HospitalNotFoundException(hospitalId));

            mockMvc.perform(get("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Hospital Not Found"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — PATIENT cannot read hospital details")
        void getById_ShouldReturn403_WhenPatient() throws Exception {
            mockMvc.perform(get("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // PUT /api/hospitals/{id} — Update
    // =========================================================================

    @Nested
    @DisplayName("PUT /api/hospitals/{id} — Update Hospital")
    class UpdateHospitalTests {

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("200 OK — HOSPITAL_ADMIN can update hospital")
        void update_ShouldReturn200_WhenHospitalAdmin() throws Exception {
            UpdateHospitalRequestDto updateDto = new UpdateHospitalRequestDto(
                    "Updated Name", null, null, null, null, null, null, null, null
            );
            when(hospitalService.updateHospital(eq(hospitalId), any(UpdateHospitalRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/api/hospitals/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(hospitalService).updateHospital(eq(hospitalId), any(UpdateHospitalRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — SUPER_ADMIN can update any hospital")
        void update_ShouldReturn200_WhenSuperAdmin() throws Exception {
            UpdateHospitalRequestDto updateDto = new UpdateHospitalRequestDto(
                    null, null, null, null, "+91-9876543210", null, "Updated description", null, null
            );
            when(hospitalService.updateHospital(eq(hospitalId), any(UpdateHospitalRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(put("/api/hospitals/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser(roles = "DOCTOR")
        @DisplayName("403 Forbidden — DOCTOR cannot update hospitals")
        void update_ShouldReturn403_WhenDoctor() throws Exception {
            UpdateHospitalRequestDto updateDto = new UpdateHospitalRequestDto(
                    "Hack Name", null, null, null, null, null, null, null, null
            );
            mockMvc.perform(put("/api/hospitals/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).updateHospital(any(), any());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 Not Found — hospital does not exist")
        void update_ShouldReturn404_WhenHospitalNotFound() throws Exception {
            UpdateHospitalRequestDto updateDto = new UpdateHospitalRequestDto(
                    "Name", null, null, null, null, null, null, null, null
            );
            when(hospitalService.updateHospital(eq(hospitalId), any()))
                    .thenThrow(new HospitalNotFoundException(hospitalId));

            mockMvc.perform(put("/api/hospitals/{id}", hospitalId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Hospital Not Found"));
        }
    }

    // =========================================================================
    // PATCH /api/hospitals/{id}/deactivate — Soft deactivate
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/hospitals/{id}/deactivate — Deactivate Hospital")
    class DeactivateHospitalTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — SUPER_ADMIN can deactivate")
        void deactivate_ShouldReturn200_WhenSuperAdmin() throws Exception {
            doNothing().when(hospitalService).deactivateHospital(hospitalId);

            mockMvc.perform(patch("/api/hospitals/{id}/deactivate", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(hospitalService).deactivateHospital(hospitalId);
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("403 Forbidden — HOSPITAL_ADMIN cannot deactivate")
        void deactivate_ShouldReturn403_WhenHospitalAdmin() throws Exception {
            mockMvc.perform(patch("/api/hospitals/{id}/deactivate", hospitalId))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).deactivateHospital(any());
        }

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("404 Not Found — hospital does not exist")
        void deactivate_ShouldReturn404_WhenHospitalNotFound() throws Exception {
            doThrow(new HospitalNotFoundException(hospitalId))
                    .when(hospitalService).deactivateHospital(hospitalId);

            mockMvc.perform(patch("/api/hospitals/{id}/deactivate", hospitalId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Hospital Not Found"));
        }
    }

    // =========================================================================
    // PATCH /api/hospitals/{id}/activate — Reactivate
    // =========================================================================

    @Nested
    @DisplayName("PATCH /api/hospitals/{id}/activate — Activate Hospital")
    class ActivateHospitalTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — SUPER_ADMIN can activate")
        void activate_ShouldReturn200_WhenSuperAdmin() throws Exception {
            doNothing().when(hospitalService).activateHospital(hospitalId);

            mockMvc.perform(patch("/api/hospitals/{id}/activate", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(hospitalService).activateHospital(hospitalId);
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("403 Forbidden — HOSPITAL_ADMIN cannot activate")
        void activate_ShouldReturn403_WhenHospitalAdmin() throws Exception {
            mockMvc.perform(patch("/api/hospitals/{id}/activate", hospitalId))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).activateHospital(any());
        }
    }

    // =========================================================================
    // DELETE /api/hospitals/{id} — RESTful soft-delete alias
    // =========================================================================

    @Nested
    @DisplayName("DELETE /api/hospitals/{id} — Soft-Delete Alias")
    class DeleteHospitalTests {

        @Test
        @WithMockUser(roles = "SUPER_ADMIN")
        @DisplayName("200 OK — SUPER_ADMIN can delete (deactivate)")
        void delete_ShouldReturn200_WhenSuperAdmin() throws Exception {
            doNothing().when(hospitalService).deactivateHospital(hospitalId);

            mockMvc.perform(delete("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(hospitalService, times(1)).deactivateHospital(hospitalId);
        }

        @Test
        @WithMockUser(roles = "HOSPITAL_ADMIN")
        @DisplayName("403 Forbidden — HOSPITAL_ADMIN cannot delete")
        void delete_ShouldReturn403_WhenHospitalAdmin() throws Exception {
            mockMvc.perform(delete("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isForbidden());

            verify(hospitalService, never()).deactivateHospital(any());
        }

        @Test
        @DisplayName("401 Unauthorized — unauthenticated request")
        void delete_ShouldReturn401_WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/hospitals/{id}", hospitalId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
