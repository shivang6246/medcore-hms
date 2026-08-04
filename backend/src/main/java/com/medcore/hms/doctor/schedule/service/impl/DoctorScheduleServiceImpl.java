package com.medcore.hms.doctor.schedule.service.impl;

import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.schedule.dto.CreateScheduleRequestDto;
import com.medcore.hms.doctor.schedule.dto.ScheduleResponseDto;
import com.medcore.hms.doctor.schedule.dto.ScheduleSummaryDto;
import com.medcore.hms.doctor.schedule.dto.UpdateScheduleRequestDto;
import com.medcore.hms.doctor.schedule.entity.DoctorSchedule;
import com.medcore.hms.doctor.schedule.exception.ScheduleConflictException;
import com.medcore.hms.doctor.schedule.exception.ScheduleNotFoundException;
import com.medcore.hms.doctor.schedule.mapper.DoctorScheduleMapper;
import com.medcore.hms.doctor.schedule.repository.DoctorScheduleRepository;
import com.medcore.hms.doctor.schedule.service.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public ScheduleResponseDto createSchedule(UUID doctorId, CreateScheduleRequestDto dto) {
        log.info("Creating schedule for doctor {} on {}", doctorId, dto.dayOfWeek());
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (scheduleRepository.existsByDoctor_IdAndDayOfWeek(doctorId, dto.dayOfWeek())) {
            log.warn("Schedule conflict — doctor {} already has schedule on {}", doctorId, dto.dayOfWeek());
            throw new ScheduleConflictException(doctorId, dto.dayOfWeek());
        }

        validateTimeWindow(dto.startTime(), dto.endTime(), dto.lunchBreakStart(), dto.lunchBreakEnd());

        DoctorSchedule schedule = DoctorSchedule.builder()
                .doctor(doctor)
                .dayOfWeek(dto.dayOfWeek())
                .startTime(dto.startTime())
                .endTime(dto.endTime())
                .lunchBreakStart(dto.lunchBreakStart())
                .lunchBreakEnd(dto.lunchBreakEnd())
                .slotDurationMinutes(dto.slotDurationMinutes() != null ? dto.slotDurationMinutes() : 30)
                .isActive(true)
                .build();

        DoctorSchedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created — ID: {}", saved.getId());
        return scheduleMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ScheduleResponseDto updateSchedule(UUID doctorId, UUID scheduleId, UpdateScheduleRequestDto dto) {
        log.info("Updating schedule {} for doctor {}", scheduleId, doctorId);
        DoctorSchedule schedule = findScheduleForDoctor(doctorId, scheduleId);

        if (dto.dayOfWeek() != null && !dto.dayOfWeek().equals(schedule.getDayOfWeek())) {
            if (scheduleRepository.existsByDoctor_IdAndDayOfWeek(doctorId, dto.dayOfWeek())) {
                throw new ScheduleConflictException(doctorId, dto.dayOfWeek());
            }
        }

        scheduleMapper.applyUpdate(dto, schedule);

        validateTimeWindow(schedule.getStartTime(), schedule.getEndTime(),
                schedule.getLunchBreakStart(), schedule.getLunchBreakEnd());

        DoctorSchedule updated = scheduleRepository.save(schedule);
        log.info("Schedule updated — ID: {}", scheduleId);
        return scheduleMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponseDto getScheduleById(UUID doctorId, UUID scheduleId) {
        log.debug("Fetching schedule {} for doctor {}", scheduleId, doctorId);
        return scheduleMapper.toResponseDto(findScheduleForDoctor(doctorId, scheduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleSummaryDto> getAllSchedulesByDoctor(UUID doctorId) {
        log.debug("Fetching all schedules for doctor {}", doctorId);
        findDoctorOrThrow(doctorId);
        return scheduleRepository.findByDoctor_Id(doctorId)
                .stream()
                .map(scheduleMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteSchedule(UUID doctorId, UUID scheduleId) {
        log.info("Deleting schedule {} for doctor {}", scheduleId, doctorId);
        DoctorSchedule schedule = findScheduleForDoctor(doctorId, scheduleId);
        scheduleRepository.delete(schedule);
        log.info("Schedule deleted — ID: {}", scheduleId);
    }

    @Override
    @Transactional
    public void activateSchedule(UUID doctorId, UUID scheduleId) {
        log.info("Activating schedule {} for doctor {}", scheduleId, doctorId);
        DoctorSchedule schedule = findScheduleForDoctor(doctorId, scheduleId);
        schedule.setIsActive(true);
        scheduleRepository.save(schedule);
    }

    @Override
    @Transactional
    public void deactivateSchedule(UUID doctorId, UUID scheduleId) {
        log.info("Deactivating schedule {} for doctor {}", scheduleId, doctorId);
        DoctorSchedule schedule = findScheduleForDoctor(doctorId, scheduleId);
        schedule.setIsActive(false);
        scheduleRepository.save(schedule);
    }

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    log.warn("Doctor not found — ID: {}", doctorId);
                    return new DoctorNotFoundException(doctorId);
                });
    }

    private DoctorSchedule findScheduleForDoctor(UUID doctorId, UUID scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.warn("Schedule not found — ID: {}", scheduleId);
                    return new ScheduleNotFoundException(scheduleId);
                });
        if (!schedule.getDoctor().getId().equals(doctorId)) {
            log.warn("Schedule {} does not belong to doctor {}", scheduleId, doctorId);
            throw new ScheduleNotFoundException(scheduleId);
        }
        return schedule;
    }

    private void validateTimeWindow(java.time.LocalTime start, java.time.LocalTime end,
                                    java.time.LocalTime lunchStart, java.time.LocalTime lunchEnd) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (lunchStart != null && lunchEnd != null) {
            if (!lunchEnd.isAfter(lunchStart)) {
                throw new IllegalArgumentException("Lunch break end must be after lunch break start");
            }
            if (lunchStart.isBefore(start) || lunchEnd.isAfter(end)) {
                throw new IllegalArgumentException("Lunch break must be within working hours");
            }
        }
    }
}
