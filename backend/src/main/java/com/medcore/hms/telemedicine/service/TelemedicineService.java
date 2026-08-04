package com.medcore.hms.telemedicine.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.telemedicine.dto.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TelemedicineService {

    TelemedicineSessionResponseDto createSession(CreateTelemedicineSessionRequestDto dto);

    TelemedicineSessionResponseDto getSessionById(UUID id);

    JoinSessionResponseDto joinWaitingRoom(UUID id, String role);

    TelemedicineSessionResponseDto startConsultation(UUID id);

    TelemedicineSessionResponseDto completeConsultation(UUID id, String notes);

    List<TelemedicineSessionSummaryDto> getDoctorWaitingRoomQueue(UUID doctorId);

    PagedResponse<TelemedicineSessionSummaryDto> getPatientConsultationHistory(UUID patientId, Pageable pageable);
}
