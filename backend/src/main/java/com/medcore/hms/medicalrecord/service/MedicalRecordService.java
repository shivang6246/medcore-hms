package com.medcore.hms.medicalrecord.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.medicalrecord.dto.CreateMedicalRecordRequestDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordResponseDto;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.dto.UpdateMedicalRecordRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicalRecordService {

    MedicalRecordResponseDto createMedicalRecord(CreateMedicalRecordRequestDto dto);

    MedicalRecordResponseDto updateMedicalRecord(UUID id, UpdateMedicalRecordRequestDto dto);

    MedicalRecordResponseDto getMedicalRecordById(UUID id);

    PagedResponse<MedicalRecordSummaryDto> getMedicalRecordsByPatient(UUID patientId, Pageable pageable);

    PagedResponse<MedicalRecordSummaryDto> getMedicalRecordsByDoctor(UUID doctorId, Pageable pageable);

    PagedResponse<MedicalRecordSummaryDto> getAllMedicalRecords(Pageable pageable);

    MedicalRecordResponseDto deactivateMedicalRecord(UUID id);
}
