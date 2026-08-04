package com.medcore.hms.pharmacy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medcore.hms.auth.jwt.JwtService;
import com.medcore.hms.auth.service.CustomUserDetailsService;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.pharmacy.dto.*;
import com.medcore.hms.pharmacy.exception.MedicineNotFoundException;
import com.medcore.hms.pharmacy.service.PharmacyService;
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
@DisplayName("PharmacyController Integration Tests")
class PharmacyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PharmacyService pharmacyService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private UUID medicineId;
    private CreateMedicineRequestDto createDto;
    private MedicineResponseDto responseDto;

    @BeforeEach
    void setUp() {
        medicineId = UUID.randomUUID();

        createDto = new CreateMedicineRequestDto(
                "Metformin 500mg", "Metformin Hydrochloride", "Glucophage", "Antidiabetic", "500mg",
                new BigDecimal("4.00"), "Merck", "89000111", 30
        );

        responseDto = new MedicineResponseDto(
                medicineId, "Metformin 500mg", "Metformin Hydrochloride", "Glucophage", "Antidiabetic", "500mg",
                new BigDecimal("4.00"), "Merck", "89000111", 100, 30, false, true,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("POST /api/medicines")
    class CreateMedicineApi {

        @Test
        @WithMockUser(roles = "PHARMACIST")
        @DisplayName("201 Created — Pharmacist creates medicine entry")
        void createMedicine_Success() throws Exception {
            when(pharmacyService.createMedicine(any(CreateMedicineRequestDto.class)))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/medicines")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(medicineId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Metformin 500mg"));
        }

        @Test
        @WithMockUser(roles = "PATIENT")
        @DisplayName("403 Forbidden — Patient cannot add medicine")
        void createMedicine_ForbiddenForPatient() throws Exception {
            mockMvc.perform(post("/api/medicines")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/inventory/add-stock & /api/dispense")
    class StockAndDispenseApi {

        @Test
        @WithMockUser(roles = "PHARMACIST")
        @DisplayName("201 Created — Add stock batch")
        void addStock_Success() throws Exception {
            AddStockBatchRequestDto addDto = new AddStockBatchRequestDto(
                    medicineId, null, "BATCH-99", LocalDate.now().plusYears(2),
                    new BigDecimal("2.50"), new BigDecimal("4.00"), 100, null
            );

            MedicineBatchResponseDto batchResponse = new MedicineBatchResponseDto(
                    UUID.randomUUID(), medicineId, "Metformin 500mg", null, "BATCH-99",
                    LocalDate.now().plusYears(2), new BigDecimal("2.50"), new BigDecimal("4.00"),
                    100, 100, false, true, LocalDateTime.now()
            );

            when(pharmacyService.addStock(any(AddStockBatchRequestDto.class))).thenReturn(batchResponse);

            mockMvc.perform(post("/api/inventory/add-stock")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.batchNumber").value("BATCH-99"));
        }

        @Test
        @WithMockUser(roles = "PHARMACIST")
        @DisplayName("201 Created — Dispense medicine")
        void dispense_Success() throws Exception {
            UUID patientId = UUID.randomUUID();
            DispenseRequestDto dispenseDto = new DispenseRequestDto(
                    patientId, null, null, null,
                    List.of(new DispenseItemRequestDto(medicineId, 5)), "Remarks"
            );

            DispenseRecordResponseDto recordResponse = new DispenseRecordResponseDto(
                    UUID.randomUUID(), "DISP-99", patientId, "Patient Name", null, "Pharmacist",
                    new BigDecimal("20.00"), LocalDateTime.now(), List.of(), "Remarks"
            );

            when(pharmacyService.dispense(any(DispenseRequestDto.class))).thenReturn(recordResponse);

            mockMvc.perform(post("/api/dispense")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dispenseDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.dispenseNumber").value("DISP-99"));
        }
    }

    @Nested
    @DisplayName("GET /api/inventory/low-stock")
    class LowStockApi {

        @Test
        @WithMockUser(roles = "PHARMACIST")
        @DisplayName("200 OK — Fetch low stock medicines")
        void getLowStockMedicines_Success() throws Exception {
            MedicineSummaryDto summaryDto = new MedicineSummaryDto(
                    medicineId, "Metformin 500mg", "Antidiabetic", "500mg", new BigDecimal("4.00"), 5, true, true
            );

            PagedResponse<MedicineSummaryDto> pagedResponse = PagedResponse.from(
                    new PageImpl<>(List.of(summaryDto), PageRequest.of(0, 10), 1)
            );

            when(pharmacyService.getLowStockMedicines(any(Pageable.class))).thenReturn(pagedResponse);

            mockMvc.perform(get("/api/inventory/low-stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].isLowStock").value(true));
        }
    }
}
