package com.medcore.hms.patient.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.patient.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PatientService {

    PatientResponseDto createPatient(CreatePatientRequestDto dto);

    PatientResponseDto updatePatient(UUID id, UpdatePatientRequestDto dto);

    PatientResponseDto getPatientById(UUID id);

    PatientResponseDto getPatientByPatientId(String patientId, UUID hospitalId);

    PagedResponse<PatientSummaryDto> getAllPatients(UUID hospitalId, Pageable pageable);

    PagedResponse<PatientSummaryDto> searchPatients(PatientSearchCriteria criteria, Pageable pageable);

    void activatePatient(UUID id);

    void deactivatePatient(UUID id);
}
