package com.medcore.hms.lab.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.lab.dto.*;
import com.medcore.hms.lab.entity.LabReport;
import com.medcore.hms.lab.entity.LabTest;
import com.medcore.hms.lab.entity.LabTestStatus;
import com.medcore.hms.lab.entity.TestPriority;
import com.medcore.hms.lab.exception.InvalidLabTestStatusException;
import com.medcore.hms.lab.exception.LabTestNotFoundException;
import com.medcore.hms.lab.mapper.LabMapper;
import com.medcore.hms.lab.repository.LabReportRepository;
import com.medcore.hms.lab.repository.LabTestRepository;
import com.medcore.hms.lab.service.impl.LabTestServiceImpl;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.user.entity.User;
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
@DisplayName("LabTestService Unit Tests")
class LabTestServiceImplTest {

    @Mock
    private LabTestRepository labTestRepository;
    @Mock
    private LabReportRepository labReportRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private LabMapper labMapper;

    @InjectMocks
    private LabTestServiceImpl labTestService;

    private UUID labTestId;
    private UUID patientId;
    private UUID doctorId;

    private Patient patient;
    private Doctor doctor;
    private LabTest labTest;
    private CreateLabTestRequestDto createDto;
    private LabTestResponseDto responseDto;

    @BeforeEach
    void setUp() {
        labTestId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        patient = Patient.builder().firstName("Alice").lastName("Smith").build();
        patient.setId(patientId);

        User doctorUser = User.builder().firstName("Bob").lastName("Jones").build();
        doctor = Doctor.builder().user(doctorUser).specialization("Pathology").build();
        doctor.setId(doctorId);

        labTest = LabTest.builder()
                .patient(patient)
                .doctor(doctor)
                .testType("Complete Blood Count (CBC)")
                .priority(TestPriority.URGENT)
                .status(LabTestStatus.REQUESTED)
                .instructions("Fasting required")
                .isActive(true)
                .build();
        labTest.setId(labTestId);

        createDto = new CreateLabTestRequestDto(
                patientId, doctorId, null, null,
                "Complete Blood Count (CBC)", TestPriority.URGENT, "Fasting required"
        );

        responseDto = new LabTestResponseDto(
                labTestId,
                new LabTestResponseDto.PatientRefDto(patientId, "P-1", "Alice", "Smith", "1234567890"),
                new LabTestResponseDto.DoctorRefDto(doctorId, "Bob", "Jones", "Pathology"),
                null, null, null, "Complete Blood Count (CBC)", TestPriority.URGENT,
                LabTestStatus.REQUESTED, "Fasting required", true, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("Create & Status Workflow Tests")
    class WorkflowTests {

        @Test
        @DisplayName("Should successfully order lab test")
        void createLabTest_Success() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(labTestRepository.save(any(LabTest.class))).thenReturn(labTest);
            when(labMapper.toLabTestResponseDto(labTest)).thenReturn(responseDto);

            LabTestResponseDto result = labTestService.createLabTest(createDto);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(labTestId);
            verify(labTestRepository).save(any(LabTest.class));
        }

        @Test
        @DisplayName("Should throw PatientNotFoundException when patient missing")
        void createLabTest_PatientNotFound() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> labTestService.createLabTest(createDto))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("Should successfully transition status from REQUESTED to SAMPLE_COLLECTED")
        void updateLabTestStatus_Success() {
            UpdateLabTestStatusRequestDto statusDto = new UpdateLabTestStatusRequestDto(LabTestStatus.SAMPLE_COLLECTED, null);

            when(labTestRepository.findById(labTestId)).thenReturn(Optional.of(labTest));
            when(labTestRepository.save(labTest)).thenReturn(labTest);
            when(labMapper.toLabTestResponseDto(labTest)).thenReturn(responseDto);

            LabTestResponseDto result = labTestService.updateLabTestStatus(labTestId, statusDto);

            assertThat(result).isNotNull();
            assertThat(labTest.getStatus()).isEqualTo(LabTestStatus.SAMPLE_COLLECTED);
        }

        @Test
        @DisplayName("Should throw InvalidLabTestStatusException when trying to modify CANCELLED test")
        void updateLabTestStatus_CancelledError() {
            labTest.setStatus(LabTestStatus.CANCELLED);
            UpdateLabTestStatusRequestDto statusDto = new UpdateLabTestStatusRequestDto(LabTestStatus.IN_PROGRESS, null);

            when(labTestRepository.findById(labTestId)).thenReturn(Optional.of(labTest));

            assertThatThrownBy(() -> labTestService.updateLabTestStatus(labTestId, statusDto))
                    .isInstanceOf(InvalidLabTestStatusException.class);
        }
    }

    @Nested
    @DisplayName("Publish Report Tests")
    class ReportTests {

        @Test
        @DisplayName("Should publish lab report and update status to COMPLETED")
        void publishLabReport_Success() {
            CreateLabReportRequestDto reportDto = new CreateLabReportRequestDto(
                    "Normal hemoglobin", "All parameters clear", "http://reports/cbc.pdf", null
            );

            LabReport labReport = LabReport.builder()
                    .labTest(labTest)
                    .result("Normal hemoglobin")
                    .remarks("All parameters clear")
                    .reportFileUrl("http://reports/cbc.pdf")
                    .reportedAt(LocalDateTime.now())
                    .build();

            LabReportResponseDto reportResponse = new LabReportResponseDto(
                    UUID.randomUUID(), labTestId, "Normal hemoglobin", "All parameters clear",
                    "http://reports/cbc.pdf", LocalDateTime.now(), null
            );

            when(labTestRepository.findById(labTestId)).thenReturn(Optional.of(labTest));
            when(labReportRepository.findByLabTest_Id(labTestId)).thenReturn(Optional.empty());
            when(labReportRepository.save(any(LabReport.class))).thenReturn(labReport);
            when(labMapper.toLabReportResponseDto(labReport)).thenReturn(reportResponse);

            LabReportResponseDto result = labTestService.publishLabReport(labTestId, reportDto);

            assertThat(result).isNotNull();
            assertThat(labTest.getStatus()).isEqualTo(LabTestStatus.COMPLETED);
            verify(labTestRepository).save(labTest);
        }
    }

    @Nested
    @DisplayName("Fetch & List Tests")
    class FetchTests {

        @Test
        @DisplayName("Should return lab test by ID")
        void getLabTestById_Success() {
            when(labTestRepository.findById(labTestId)).thenReturn(Optional.of(labTest));
            when(labMapper.toLabTestResponseDto(labTest)).thenReturn(responseDto);

            LabTestResponseDto result = labTestService.getLabTestById(labTestId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(labTestId);
        }

        @Test
        @DisplayName("Should throw LabTestNotFoundException when ID invalid")
        void getLabTestById_NotFound() {
            when(labTestRepository.findById(labTestId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> labTestService.getLabTestById(labTestId))
                    .isInstanceOf(LabTestNotFoundException.class);
        }

        @Test
        @DisplayName("Should list lab tests by Patient ID")
        void getLabTestsByPatient_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<LabTest> page = new PageImpl<>(List.of(labTest));
            LabTestSummaryDto summaryDto = new LabTestSummaryDto(
                    labTestId, "Alice Smith", "Bob Jones", "CBC", TestPriority.URGENT, LabTestStatus.REQUESTED, LocalDateTime.now()
            );

            when(patientRepository.existsById(patientId)).thenReturn(true);
            when(labTestRepository.findByPatient_Id(patientId, pageable)).thenReturn(page);
            when(labMapper.toLabTestSummaryDto(labTest)).thenReturn(summaryDto);

            PagedResponse<LabTestSummaryDto> response = labTestService.getLabTestsByPatient(patientId, pageable);

            assertThat(response).isNotNull();
            assertThat(response.content()).hasSize(1);
        }
    }
}
