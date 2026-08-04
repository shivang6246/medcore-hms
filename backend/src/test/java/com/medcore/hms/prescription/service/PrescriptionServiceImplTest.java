package com.medcore.hms.prescription.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequestDto;
import com.medcore.hms.prescription.dto.PrescriptionResponseDto;
import com.medcore.hms.prescription.dto.PrescriptionSummaryDto;
import com.medcore.hms.prescription.dto.UpdatePrescriptionRequestDto;
import com.medcore.hms.prescription.entity.Prescription;
import com.medcore.hms.prescription.exception.PrescriptionNotFoundException;
import com.medcore.hms.prescription.mapper.PrescriptionMapper;
import com.medcore.hms.prescription.repository.PrescriptionRepository;
import com.medcore.hms.prescription.service.impl.PrescriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrescriptionService Unit Tests")
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private PrescriptionMapper prescriptionMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private UUID prescriptionId;
    private UUID medicalRecordId;

    private MedicalRecord medicalRecord;
    private Prescription prescription;
    private CreatePrescriptionRequestDto createDto;
    private PrescriptionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        prescriptionId = UUID.randomUUID();
        medicalRecordId = UUID.randomUUID();

        medicalRecord = MedicalRecord.builder().symptoms("Fever").diagnosis("Flu").build();
        medicalRecord.setId(medicalRecordId);

        prescription = Prescription.builder()
                .medicalRecord(medicalRecord)
                .medicineName("Paracetamol")
                .dosage("500mg")
                .frequency("1-0-1")
                .duration(5)
                .instructions("Take after food")
                .quantity(10)
                .isActive(true)
                .build();
        prescription.setId(prescriptionId);

        createDto = new CreatePrescriptionRequestDto(
                medicalRecordId, "Paracetamol", "500mg", "1-0-1", 5, "Take after food", 10
        );

        responseDto = new PrescriptionResponseDto(
                prescriptionId, medicalRecordId, "Paracetamol", "500mg", "1-0-1", 5,
                "Take after food", 10, true, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Create Prescription Tests")
    class CreateTests {

        @Test
        @DisplayName("Should successfully create prescription")
        void createPrescription_Success() {
            when(medicalRecordRepository.findById(medicalRecordId)).thenReturn(Optional.of(medicalRecord));
            when(prescriptionMapper.toEntity(createDto, medicalRecord)).thenReturn(prescription);
            when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(responseDto);

            PrescriptionResponseDto result = prescriptionService.createPrescription(createDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(prescriptionId);
            verify(prescriptionRepository).save(any(Prescription.class));
        }

        @Test
        @DisplayName("Should throw MedicalRecordNotFoundException when medical record missing")
        void createPrescription_MedicalRecordNotFound() {
            when(medicalRecordRepository.findById(medicalRecordId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> prescriptionService.createPrescription(createDto))
                    .isInstanceOf(MedicalRecordNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Fetch & List Tests")
    class FetchTests {

        @Test
        @DisplayName("Should return prescription by ID")
        void getPrescriptionById_Success() {
            when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription));
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(responseDto);

            PrescriptionResponseDto result = prescriptionService.getPrescriptionById(prescriptionId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(prescriptionId);
        }

        @Test
        @DisplayName("Should throw PrescriptionNotFoundException when ID invalid")
        void getPrescriptionById_NotFound() {
            when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> prescriptionService.getPrescriptionById(prescriptionId))
                    .isInstanceOf(PrescriptionNotFoundException.class);
        }

        @Test
        @DisplayName("Should list prescriptions by Medical Record ID")
        void getPrescriptionsByMedicalRecord_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Prescription> page = new PageImpl<>(List.of(prescription));
            PrescriptionSummaryDto summaryDto = new PrescriptionSummaryDto(
                    prescriptionId, "Paracetamol", "500mg", "1-0-1", 5, true
            );

            when(medicalRecordRepository.existsById(medicalRecordId)).thenReturn(true);
            when(prescriptionRepository.findByMedicalRecord_Id(medicalRecordId, pageable)).thenReturn(page);
            when(prescriptionMapper.toSummaryDto(prescription)).thenReturn(summaryDto);

            PagedResponse<PrescriptionSummaryDto> response = prescriptionService.getPrescriptionsByMedicalRecord(medicalRecordId, pageable);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update & Deactivate Tests")
    class ModifyTests {

        @Test
        @DisplayName("Should update prescription successfully")
        void updatePrescription_Success() {
            UpdatePrescriptionRequestDto updateDto = new UpdatePrescriptionRequestDto(
                    "Ibuprofen", "400mg", "Once daily", 3, "After breakfast", 3
            );

            when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(prescription)).thenReturn(prescription);
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(responseDto);

            PrescriptionResponseDto result = prescriptionService.updatePrescription(prescriptionId, updateDto);

            assertThat(result).isNotNull();
            verify(prescriptionMapper).updateEntity(eq(prescription), eq(updateDto));
            verify(prescriptionRepository).save(prescription);
        }

        @Test
        @DisplayName("Should deactivate prescription")
        void deactivatePrescription_Success() {
            when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription));
            when(prescriptionRepository.save(prescription)).thenReturn(prescription);
            when(prescriptionMapper.toResponseDto(prescription)).thenReturn(responseDto);

            PrescriptionResponseDto result = prescriptionService.deactivatePrescription(prescriptionId);

            assertThat(result).isNotNull();
            assertThat(prescription.isActive()).isFalse();
            verify(prescriptionRepository).save(prescription);
        }
    }
}
