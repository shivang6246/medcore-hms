package com.medcore.hms.lab.service.impl;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
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
import com.medcore.hms.lab.service.LabTestService;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabTestServiceImpl implements LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabReportRepository labReportRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserRepository userRepository;
    private final LabMapper labMapper;

    @Override
    @Transactional
    public LabTestResponseDto createLabTest(CreateLabTestRequestDto dto) {
        log.info("Creating lab test request for patient ID: {}, doctor ID: {}, testType: '{}'",
                dto.patientId(), dto.doctorId(), dto.testType());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.patientId()));

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(dto.doctorId()));

        Appointment appointment = null;
        if (dto.appointmentId() != null) {
            appointment = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new AppointmentNotFoundException(dto.appointmentId()));
        }

        MedicalRecord medicalRecord = null;
        if (dto.medicalRecordId() != null) {
            medicalRecord = medicalRecordRepository.findById(dto.medicalRecordId())
                    .orElseThrow(() -> new MedicalRecordNotFoundException(dto.medicalRecordId()));
        }

        TestPriority priority = dto.priority() != null ? dto.priority() : TestPriority.NORMAL;

        LabTest labTest = LabTest.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .medicalRecord(medicalRecord)
                .testType(dto.testType())
                .priority(priority)
                .status(LabTestStatus.REQUESTED)
                .instructions(dto.instructions())
                .isActive(true)
                .build();

        LabTest savedLabTest = labTestRepository.save(labTest);
        log.info("Lab test ordered successfully with ID: {}", savedLabTest.getId());
        return labMapper.toLabTestResponseDto(savedLabTest);
    }

    @Override
    @Transactional
    public LabTestResponseDto updateLabTestStatus(UUID id, UpdateLabTestStatusRequestDto dto) {
        log.info("Updating lab test ID: {} to status: {}", id, dto.status());

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new LabTestNotFoundException(id));

        if (labTest.getStatus() == LabTestStatus.COMPLETED && dto.status() != LabTestStatus.COMPLETED) {
            throw new InvalidLabTestStatusException("Cannot alter status of a COMPLETED lab test.");
        }

        if (labTest.getStatus() == LabTestStatus.CANCELLED) {
            throw new InvalidLabTestStatusException("Cannot update status of a CANCELLED lab test.");
        }

        labTest.setStatus(dto.status());

        if (dto.technicianId() != null) {
            User tech = userRepository.findById(dto.technicianId())
                    .orElseThrow(() -> new IllegalArgumentException("Technician user not found with ID: " + dto.technicianId()));
            labTest.setTechnician(tech);
        }

        LabTest updatedTest = labTestRepository.save(labTest);
        return labMapper.toLabTestResponseDto(updatedTest);
    }

    @Override
    @Transactional
    public LabReportResponseDto publishLabReport(UUID labTestId, CreateLabReportRequestDto dto) {
        log.info("Publishing lab report for lab test ID: {}", labTestId);

        LabTest labTest = labTestRepository.findById(labTestId)
                .orElseThrow(() -> new LabTestNotFoundException(labTestId));

        if (labTest.getStatus() == LabTestStatus.CANCELLED) {
            throw new InvalidLabTestStatusException("Cannot publish report for a CANCELLED lab test.");
        }

        User reportedBy = null;
        if (dto.reportedById() != null) {
            reportedBy = userRepository.findById(dto.reportedById()).orElse(null);
        } else if (labTest.getTechnician() != null) {
            reportedBy = labTest.getTechnician();
        }

        LabReport labReport = labReportRepository.findByLabTest_Id(labTestId)
                .orElseGet(() -> LabReport.builder().labTest(labTest).build());

        labReport.setResult(dto.result());
        labReport.setRemarks(dto.remarks());
        labReport.setReportFileUrl(dto.reportFileUrl());
        labReport.setReportedAt(LocalDateTime.now());
        labReport.setReportedBy(reportedBy);

        LabReport savedReport = labReportRepository.save(labReport);

        // Update lab test status to COMPLETED
        labTest.setStatus(LabTestStatus.COMPLETED);
        labTest.setLabReport(savedReport);
        labTestRepository.save(labTest);

        log.info("Lab report published successfully with ID: {} for lab test ID: {}", savedReport.getId(), labTestId);
        return labMapper.toLabReportResponseDto(savedReport);
    }

    @Override
    public LabTestResponseDto getLabTestById(UUID id) {
        log.info("Fetching lab test by ID: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> new LabTestNotFoundException(id));

        return labMapper.toLabTestResponseDto(labTest);
    }

    @Override
    public PagedResponse<LabTestSummaryDto> getLabTestsByPatient(UUID patientId, Pageable pageable) {
        log.info("Fetching lab tests for patient ID: {}", patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException(patientId);
        }

        Page<LabTest> page = labTestRepository.findByPatient_Id(patientId, pageable);
        return PagedResponse.from(page.map(labMapper::toLabTestSummaryDto));
    }

    @Override
    public PagedResponse<LabTestSummaryDto> getLabTestsByDoctor(UUID doctorId, Pageable pageable) {
        log.info("Fetching lab tests for doctor ID: {}", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }

        Page<LabTest> page = labTestRepository.findByDoctor_Id(doctorId, pageable);
        return PagedResponse.from(page.map(labMapper::toLabTestSummaryDto));
    }

    @Override
    public PagedResponse<LabTestSummaryDto> getLabTestsByStatus(LabTestStatus status, Pageable pageable) {
        log.info("Fetching lab tests by status: {}", status);

        Page<LabTest> page = labTestRepository.findByStatus(status, pageable);
        return PagedResponse.from(page.map(labMapper::toLabTestSummaryDto));
    }

    @Override
    public PagedResponse<LabTestSummaryDto> getAllLabTests(Pageable pageable) {
        log.info("Fetching all lab tests paginated");

        Page<LabTest> page = labTestRepository.findAll(pageable);
        return PagedResponse.from(page.map(labMapper::toLabTestSummaryDto));
    }
}
