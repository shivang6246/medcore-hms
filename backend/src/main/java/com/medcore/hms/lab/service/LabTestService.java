package com.medcore.hms.lab.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.lab.dto.*;
import com.medcore.hms.lab.entity.LabTestStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LabTestService {

    LabTestResponseDto createLabTest(CreateLabTestRequestDto dto);

    LabTestResponseDto updateLabTestStatus(UUID id, UpdateLabTestStatusRequestDto dto);

    LabReportResponseDto publishLabReport(UUID labTestId, CreateLabReportRequestDto dto);

    LabTestResponseDto getLabTestById(UUID id);

    PagedResponse<LabTestSummaryDto> getLabTestsByPatient(UUID patientId, Pageable pageable);

    PagedResponse<LabTestSummaryDto> getLabTestsByDoctor(UUID doctorId, Pageable pageable);

    PagedResponse<LabTestSummaryDto> getLabTestsByStatus(LabTestStatus status, Pageable pageable);

    PagedResponse<LabTestSummaryDto> getAllLabTests(Pageable pageable);
}
