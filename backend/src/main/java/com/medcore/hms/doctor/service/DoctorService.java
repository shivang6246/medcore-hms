package com.medcore.hms.doctor.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.dto.CreateDoctorRequestDto;
import com.medcore.hms.doctor.dto.DoctorResponseDto;
import com.medcore.hms.doctor.dto.DoctorSummaryDto;
import com.medcore.hms.doctor.dto.UpdateDoctorRequestDto;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface DoctorService {

    DoctorResponseDto createDoctor(CreateDoctorRequestDto dto);

    DoctorResponseDto updateDoctor(UUID id, UpdateDoctorRequestDto dto);

    DoctorResponseDto getDoctorById(UUID id);

    PagedResponse<DoctorSummaryDto> getAllDoctors(Pageable pageable);

    DoctorResponseDto getDoctorByUserId(UUID userId);

    void activateDoctor(UUID id);

    void deactivateDoctor(UUID id);

    DoctorResponseDto assignDepartment(UUID doctorId, UUID departmentId);

    DoctorResponseDto updateConsultationFee(UUID doctorId, BigDecimal fee);

    void updateAvailability(UUID doctorId, boolean available);
}
