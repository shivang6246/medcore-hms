package com.medcore.hms.prescription.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequestDto;
import com.medcore.hms.prescription.dto.PrescriptionResponseDto;
import com.medcore.hms.prescription.dto.PrescriptionSummaryDto;
import com.medcore.hms.prescription.dto.UpdatePrescriptionRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PrescriptionService {

    PrescriptionResponseDto createPrescription(CreatePrescriptionRequestDto dto);

    PrescriptionResponseDto updatePrescription(UUID id, UpdatePrescriptionRequestDto dto);

    PrescriptionResponseDto getPrescriptionById(UUID id);

    PagedResponse<PrescriptionSummaryDto> getPrescriptionsByMedicalRecord(UUID medicalRecordId, Pageable pageable);

    List<PrescriptionResponseDto> getPrescriptionListByMedicalRecord(UUID medicalRecordId);

    PagedResponse<PrescriptionSummaryDto> getAllPrescriptions(Pageable pageable);

    PrescriptionResponseDto deactivatePrescription(UUID id);
}
