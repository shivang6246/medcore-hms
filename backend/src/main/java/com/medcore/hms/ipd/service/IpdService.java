package com.medcore.hms.ipd.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.AdmissionStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IpdService {

    AdmissionResponseDto createAdmission(CreateAdmissionRequestDto dto);

    AdmissionResponseDto getAdmissionById(UUID id);

    AdmissionResponseDto transferBed(UUID admissionId, TransferBedRequestDto dto);

    AdmissionResponseDto dischargePatient(UUID admissionId, DischargeRequestDto dto);

    PagedResponse<BedResponseDto> getAvailableBeds(UUID wardId, Pageable pageable);

    PagedResponse<AdmissionSummaryDto> getAllAdmissions(AdmissionStatus status, Pageable pageable);
}
