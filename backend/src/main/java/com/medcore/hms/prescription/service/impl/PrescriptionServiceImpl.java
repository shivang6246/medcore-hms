package com.medcore.hms.prescription.service.impl;

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
import com.medcore.hms.prescription.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionMapper prescriptionMapper;

    @Override
    @Transactional
    public PrescriptionResponseDto createPrescription(CreatePrescriptionRequestDto dto) {
        log.info("Creating prescription for medicine: '{}' linked to medical record ID: {}",
                dto.medicineName(), dto.medicalRecordId());

        MedicalRecord medicalRecord = medicalRecordRepository.findById(dto.medicalRecordId())
                .orElseThrow(() -> new MedicalRecordNotFoundException(dto.medicalRecordId()));

        Prescription entity = prescriptionMapper.toEntity(dto, medicalRecord);
        Prescription savedEntity = prescriptionRepository.save(entity);

        log.info("Prescription created with ID: {}", savedEntity.getId());
        return prescriptionMapper.toResponseDto(savedEntity);
    }

    @Override
    @Transactional
    public PrescriptionResponseDto updatePrescription(UUID id, UpdatePrescriptionRequestDto dto) {
        log.info("Updating prescription ID: {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(id));

        prescriptionMapper.updateEntity(prescription, dto);
        Prescription updatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription ID: {} updated successfully", id);
        return prescriptionMapper.toResponseDto(updatedPrescription);
    }

    @Override
    public PrescriptionResponseDto getPrescriptionById(UUID id) {
        log.info("Fetching prescription by ID: {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(id));

        return prescriptionMapper.toResponseDto(prescription);
    }

    @Override
    public PagedResponse<PrescriptionSummaryDto> getPrescriptionsByMedicalRecord(UUID medicalRecordId, Pageable pageable) {
        log.info("Fetching prescriptions for medical record ID: {}", medicalRecordId);

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new MedicalRecordNotFoundException(medicalRecordId);
        }

        Page<Prescription> page = prescriptionRepository.findByMedicalRecord_Id(medicalRecordId, pageable);
        return PagedResponse.from(page.map(prescriptionMapper::toSummaryDto));
    }

    @Override
    public List<PrescriptionResponseDto> getPrescriptionListByMedicalRecord(UUID medicalRecordId) {
        log.info("Fetching list of all prescriptions for medical record ID: {}", medicalRecordId);

        if (!medicalRecordRepository.existsById(medicalRecordId)) {
            throw new MedicalRecordNotFoundException(medicalRecordId);
        }

        List<Prescription> list = prescriptionRepository.findByMedicalRecord_Id(medicalRecordId);
        return list.stream().map(prescriptionMapper::toResponseDto).toList();
    }

    @Override
    public PagedResponse<PrescriptionSummaryDto> getAllPrescriptions(Pageable pageable) {
        log.info("Fetching all prescriptions paginated");

        Page<Prescription> page = prescriptionRepository.findAll(pageable);
        return PagedResponse.from(page.map(prescriptionMapper::toSummaryDto));
    }

    @Override
    @Transactional
    public PrescriptionResponseDto deactivatePrescription(UUID id) {
        log.info("Deactivating prescription ID: {}", id);

        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(id));

        prescription.setActive(false);
        Prescription deactivatedPrescription = prescriptionRepository.save(prescription);

        log.info("Prescription ID: {} deactivated successfully", id);
        return prescriptionMapper.toResponseDto(deactivatedPrescription);
    }
}
