package com.medcore.hms.patient.service;

import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.patient.dto.*;
import com.medcore.hms.patient.entity.BloodGroup;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.DuplicatePatientEmailException;
import com.medcore.hms.patient.exception.DuplicatePatientPhoneException;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.mapper.PatientMapper;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.patient.service.impl.PatientServiceImpl;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PatientService Unit Tests")
class PatientServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private PatientMapper patientMapper;

    @InjectMocks private PatientServiceImpl patientService;

    private UUID hospitalId;
    private UUID patientUUID;
    private Hospital hospital;
    private Patient patient;

    @BeforeEach
    void setUp() {
        hospitalId  = UUID.randomUUID();
        patientUUID = UUID.randomUUID();

        hospital = Hospital.builder()
                .name("Test Hospital")
                .registrationNumber("REG-001")
                .licenseNumber("LIC-001")
                .email("hosp@test.com")
                .isActive(true)
                .build();

        patient = Patient.builder()
                .hospital(hospital)
                .patientId("P-2026-00001")
                .firstName("Aanya")
                .lastName("Mehta")
                .dateOfBirth(LocalDate.of(1992, 5, 14))
                .phone("+91-9811223344")
                .email("aanya@test.com")
                .emergencyContactName("Rajiv Mehta")
                .emergencyContactPhone("+91-9822334455")
                .bloodGroup(BloodGroup.B_POSITIVE)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createPatient")
    class CreatePatientTests {

        @Test
        @DisplayName("should create patient successfully")
        void createPatient_success() {
            CreatePatientRequestDto dto = new CreatePatientRequestDto(
                    hospitalId, "Aanya", "Mehta",
                    LocalDate.of(1992, 5, 14), null, BloodGroup.B_POSITIVE,
                    "+91-9811223344", "aanya@test.com", null,
                    "Rajiv Mehta", "+91-9822334455", "Spouse",
                    null, null, null, null
            );

            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.existsByPhoneAndHospital_Id(any(), any())).thenReturn(false);
            when(patientRepository.existsByEmail(any())).thenReturn(false);
            when(patientRepository.findMaxPatientIdByHospitalAndPrefix(eq(hospitalId), any())).thenReturn(Optional.empty());
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);
            when(patientMapper.toResponseDto(patient)).thenReturn(mockResponseDto());

            PatientResponseDto result = patientService.createPatient(dto);

            assertThat(result).isNotNull();
            verify(patientRepository).save(any(Patient.class));
        }

        @Test
        @DisplayName("should throw DuplicatePatientPhoneException when phone already exists in hospital")
        void createPatient_duplicatePhone_throwsException() {
            CreatePatientRequestDto dto = new CreatePatientRequestDto(
                    hospitalId, "Aanya", "Mehta",
                    LocalDate.of(1992, 5, 14), null, null,
                    "+91-9811223344", null, null,
                    "Rajiv", "+91-9822334455", null,
                    null, null, null, null
            );

            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.existsByPhoneAndHospital_Id("+91-9811223344", hospitalId)).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(dto))
                    .isInstanceOf(DuplicatePatientPhoneException.class)
                    .hasMessageContaining("+91-9811223344");

            verify(patientRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicatePatientEmailException when email already registered")
        void createPatient_duplicateEmail_throwsException() {
            CreatePatientRequestDto dto = new CreatePatientRequestDto(
                    hospitalId, "Aanya", "Mehta",
                    LocalDate.of(1992, 5, 14), null, null,
                    "+91-9811223344", "aanya@test.com", null,
                    "Rajiv", "+91-9822334455", null,
                    null, null, null, null
            );

            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.existsByPhoneAndHospital_Id(any(), any())).thenReturn(false);
            when(patientRepository.existsByEmail("aanya@test.com")).thenReturn(true);

            assertThatThrownBy(() -> patientService.createPatient(dto))
                    .isInstanceOf(DuplicatePatientEmailException.class)
                    .hasMessageContaining("aanya@test.com");

            verify(patientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getPatientById")
    class GetPatientByIdTests {

        @Test
        @DisplayName("should return patient when found")
        void getPatientById_found() {
            when(patientRepository.findById(patientUUID)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponseDto(patient)).thenReturn(mockResponseDto());

            PatientResponseDto result = patientService.getPatientById(patientUUID);

            assertThat(result).isNotNull();
            verify(patientRepository).findById(patientUUID);
        }

        @Test
        @DisplayName("should throw PatientNotFoundException when not found")
        void getPatientById_notFound() {
            when(patientRepository.findById(patientUUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.getPatientById(patientUUID))
                    .isInstanceOf(PatientNotFoundException.class)
                    .hasMessageContaining(patientUUID.toString());
        }
    }

    @Nested
    @DisplayName("getAllPatients")
    class GetAllPatientsTests {

        @Test
        @DisplayName("should return paginated results")
        void getAllPatients_returnsPaged() {
            Page<Patient> page = new PageImpl<>(List.of(patient));
            when(patientRepository.findByHospital_Id(eq(hospitalId), any())).thenReturn(page);
            when(patientMapper.toSummaryDto(patient)).thenReturn(mockSummaryDto());

            var result = patientService.getAllPatients(hospitalId, PageRequest.of(0, 10));

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("activatePatient / deactivatePatient")
    class LifecycleTests {

        @Test
        @DisplayName("should activate an inactive patient")
        void activatePatient_success() {
            patient.setIsActive(false);
            when(patientRepository.findById(patientUUID)).thenReturn(Optional.of(patient));
            when(patientRepository.save(patient)).thenReturn(patient);

            patientService.activatePatient(patientUUID);

            assertThat(patient.getIsActive()).isTrue();
            verify(patientRepository).save(patient);
        }

        @Test
        @DisplayName("should deactivate an active patient")
        void deactivatePatient_success() {
            when(patientRepository.findById(patientUUID)).thenReturn(Optional.of(patient));
            when(patientRepository.save(patient)).thenReturn(patient);

            patientService.deactivatePatient(patientUUID);

            assertThat(patient.getIsActive()).isFalse();
            verify(patientRepository).save(patient);
        }

        @Test
        @DisplayName("should throw PatientNotFoundException on activate of unknown patient")
        void activatePatient_notFound() {
            when(patientRepository.findById(patientUUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> patientService.activatePatient(patientUUID))
                    .isInstanceOf(PatientNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updatePatient")
    class UpdatePatientTests {

        @Test
        @DisplayName("should update patient successfully")
        void updatePatient_success() {
            UpdatePatientRequestDto dto = new UpdatePatientRequestDto(
                    "Aanya", "Singh", null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null
            );

            when(patientRepository.findById(patientUUID)).thenReturn(Optional.of(patient));
            when(patientRepository.save(patient)).thenReturn(patient);
            when(patientMapper.toResponseDto(patient)).thenReturn(mockResponseDto());

            PatientResponseDto result = patientService.updatePatient(patientUUID, dto);

            assertThat(result).isNotNull();
            verify(patientMapper).applyUpdate(dto, patient);
        }
    }

    private PatientResponseDto mockResponseDto() {
        return new PatientResponseDto(
                patientUUID, "P-2026-00001",
                "Aanya", "Mehta",
                LocalDate.of(1992, 5, 14),
                null, BloodGroup.B_POSITIVE,
                "+91-9811223344", "aanya@test.com",
                null,
                new EmergencyContactDto("Rajiv Mehta", "+91-9822334455", "Spouse"),
                null, null, null, null,
                true,
                new com.medcore.hms.doctor.dto.HospitalRefDto(hospitalId, "Test Hospital"),
                null, null
        );
    }

    private PatientSummaryDto mockSummaryDto() {
        return new PatientSummaryDto(
                patientUUID, "P-2026-00001",
                "Aanya", "Mehta",
                LocalDate.of(1992, 5, 14),
                "+91-9811223344", "aanya@test.com",
                BloodGroup.B_POSITIVE, true, "Test Hospital"
        );
    }
}
