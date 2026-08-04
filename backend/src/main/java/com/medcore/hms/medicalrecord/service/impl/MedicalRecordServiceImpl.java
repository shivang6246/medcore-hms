package com.medcore.hms.medicalrecord.service.impl;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.exception.AppointmentNotFoundException;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.medicalrecord.exception.AppointmentMismatchException;
import com.medcore.hms.medicalrecord.exception.DuplicateMedicalRecordException;
import com.medcore.hms.medicalrecord.exception.MedicalRecordNotFoundException;
import com.medcore.hms.medicalrecord.mapper.MedicalRecordMapper;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.medicalrecord.service.MedicalRecordService;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    @Transactional
    public MedicalRecordResponseDto createMedicalRecord(CreateMedicalRecordRequestDto dto) {
        log.info("Creating medical record for patient: {}, doctor: {}, appointment: {}",
                dto.patientId(), dto.doctorId(), dto.appointmentId());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.patientId()));

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(dto.doctorId()));

        Appointment appointment = appointmentRepository.findById(dto.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(dto.appointmentId()));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new AppointmentMismatchException("Appointment " + dto.appointmentId() + " does not belong to patient " + dto.patientId());
        }

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new AppointmentMismatchException("Appointment " + dto.appointmentId() + " does not belong to doctor " + dto.doctorId());
        }

        if (medicalRecordRepository.existsByAppointment_Id(dto.appointmentId())) {
            throw new DuplicateMedicalRecordException(dto.appointmentId());
        }

        MedicalRecord entity = medicalRecordMapper.toEntity(dto, patient, doctor, appointment);
        MedicalRecord savedEntity = medicalRecordRepository.save(entity);

        log.info("Medical record created with ID: {}", savedEntity.getId());
        return medicalRecordMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public MedicalRecordResponseDto updateMedicalRecord(UUID id, UpdateMedicalRecordRequestDto dto) {
        log.info("Updating medical record ID: {}", id);

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException(id));

        medicalRecordMapper.updateEntity(record, dto);
        MedicalRecord updatedRecord = medicalRecordRepository.save(record);

        log.info("Medical record ID: {} updated successfully", id);
        return medicalRecordMapper.toResponseDto(updatedRecord);
    }

    @Override
    public MedicalRecordResponseDto getMedicalRecordById(UUID id) {
        log.info("Fetching medical record by ID: {}", id);

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException(id));

        return medicalRecordMapper.toResponseDto(record);
    }

    @Override
    public PagedResponse<MedicalRecordSummaryDto> getMedicalRecordsByPatient(UUID patientId, Pageable pageable) {
        log.info("Fetching medical records for patient ID: {}", patientId);

        if (!patientRepository.existsById(patientId)) {
            throw new PatientNotFoundException(patientId);
        }

        Page<MedicalRecord> page = medicalRecordRepository.findByPatient_Id(patientId, pageable);
        return PagedResponse.from(page.map(medicalRecordMapper::toSummaryDto));
    }

    @Override
    public PagedResponse<MedicalRecordSummaryDto> getMedicalRecordsByDoctor(UUID doctorId, Pageable pageable) {
        log.info("Fetching medical records for doctor ID: {}", doctorId);

        if (!doctorRepository.existsById(doctorId)) {
            throw new DoctorNotFoundException(doctorId);
        }

        Page<MedicalRecord> page = medicalRecordRepository.findByDoctor_Id(doctorId, pageable);
        return PagedResponse.from(page.map(medicalRecordMapper::toSummaryDto));
    }

    @Override
    public PagedResponse<MedicalRecordSummaryDto> getAllMedicalRecords(Pageable pageable) {
        log.info("Fetching all medical records paginated");

        Page<MedicalRecord> page = medicalRecordRepository.findAll(pageable);
        return PagedResponse.from(page.map(medicalRecordMapper::toSummaryDto));
    }

    @Override
    @Transactional
    public MedicalRecordResponseDto deactivateMedicalRecord(UUID id) {
        log.info("Deactivating medical record ID: {}", id);

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new MedicalRecordNotFoundException(id));

        record.setActive(false);
        MedicalRecord deactivatedRecord = medicalRecordRepository.save(record);

        log.info("Medical record ID: {} deactivated successfully", id);
        return medicalRecordMapper.toResponseDto(deactivatedRecord);
    }
}
