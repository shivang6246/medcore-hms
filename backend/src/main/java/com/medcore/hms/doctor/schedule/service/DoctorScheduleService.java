package com.medcore.hms.doctor.schedule.service;

import com.medcore.hms.doctor.schedule.dto.CreateScheduleRequestDto;
import com.medcore.hms.doctor.schedule.dto.ScheduleResponseDto;
import com.medcore.hms.doctor.schedule.dto.ScheduleSummaryDto;
import com.medcore.hms.doctor.schedule.dto.UpdateScheduleRequestDto;

import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {

    ScheduleResponseDto createSchedule(UUID doctorId, CreateScheduleRequestDto dto);

    ScheduleResponseDto updateSchedule(UUID doctorId, UUID scheduleId, UpdateScheduleRequestDto dto);

    ScheduleResponseDto getScheduleById(UUID doctorId, UUID scheduleId);

    List<ScheduleSummaryDto> getAllSchedulesByDoctor(UUID doctorId);

    void deleteSchedule(UUID doctorId, UUID scheduleId);

    void activateSchedule(UUID doctorId, UUID scheduleId);

    void deactivateSchedule(UUID doctorId, UUID scheduleId);
}
