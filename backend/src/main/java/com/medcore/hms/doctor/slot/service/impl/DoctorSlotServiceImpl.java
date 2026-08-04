package com.medcore.hms.doctor.slot.service.impl;

import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.schedule.entity.DayOfWeek;
import com.medcore.hms.doctor.schedule.entity.DoctorSchedule;
import com.medcore.hms.doctor.schedule.repository.DoctorScheduleRepository;
import com.medcore.hms.doctor.slot.dto.BlockSlotRequestDto;
import com.medcore.hms.doctor.slot.dto.GenerateSlotsRequestDto;
import com.medcore.hms.doctor.slot.dto.SlotResponseDto;
import com.medcore.hms.doctor.slot.dto.SlotSummaryDto;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.doctor.slot.entity.SlotStatus;
import com.medcore.hms.doctor.slot.exception.SlotAlreadyBookedException;
import com.medcore.hms.doctor.slot.exception.SlotGenerationException;
import com.medcore.hms.doctor.slot.exception.SlotNotFoundException;
import com.medcore.hms.doctor.slot.mapper.DoctorSlotMapper;
import com.medcore.hms.doctor.slot.repository.DoctorSlotRepository;
import com.medcore.hms.doctor.slot.service.DoctorSlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorSlotServiceImpl implements DoctorSlotService {

    private static final int MAX_GENERATION_DAYS = 90;

    private final DoctorSlotRepository slotRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorSlotMapper slotMapper;

    @Override
    @Transactional
    public List<SlotSummaryDto> generateSlots(UUID doctorId, GenerateSlotsRequestDto dto) {
        log.info("Generating slots for doctor {} from {} to {}", doctorId, dto.fromDate(), dto.toDate());

        Doctor doctor = findDoctorOrThrow(doctorId);
        validateDateRange(dto.fromDate(), dto.toDate());

        List<DoctorSlot> generated = new ArrayList<>();
        LocalDate current = dto.fromDate();

        while (!current.isAfter(dto.toDate())) {
            DayOfWeek dayOfWeek = mapToDayOfWeek(current.getDayOfWeek());
            Optional<DoctorSchedule> scheduleOpt = scheduleRepository.findByDoctor_IdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);

            if (scheduleOpt.isPresent()) {
                DoctorSchedule schedule = scheduleOpt.get();
                List<DoctorSlot> daySlots = generateDaySlots(doctor, schedule, current);
                generated.addAll(daySlots);
            }

            current = current.plusDays(1);
        }

        if (generated.isEmpty()) {
            log.info("No active schedules found for doctor {} in the given range — no slots generated", doctorId);
        } else {
            slotRepository.saveAll(generated);
            log.info("Generated {} slots for doctor {}", generated.size(), doctorId);
        }

        return generated.stream().map(slotMapper::toSummaryDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotSummaryDto> getSlotsByDoctorAndDate(UUID doctorId, LocalDate date) {
        log.debug("Fetching slots for doctor {} on {}", doctorId, date);
        findDoctorOrThrow(doctorId);
        return slotRepository.findByDoctor_IdAndSlotDateOrderByStartTime(doctorId, date)
                .stream()
                .map(slotMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotSummaryDto> getSlotsByDoctorAndDateRange(UUID doctorId, LocalDate from, LocalDate to) {
        log.debug("Fetching slots for doctor {} from {} to {}", doctorId, from, to);
        findDoctorOrThrow(doctorId);
        validateDateRange(from, to);
        return slotRepository.findByDoctor_IdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(doctorId, from, to)
                .stream()
                .map(slotMapper::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SlotResponseDto getSlotById(UUID slotId) {
        log.debug("Fetching slot {}", slotId);
        return slotMapper.toResponseDto(findSlotOrThrow(slotId));
    }

    @Override
    @Transactional
    public SlotResponseDto blockSlot(UUID slotId, BlockSlotRequestDto dto) {
        log.info("Blocking slot {}", slotId);
        DoctorSlot slot = findSlotOrThrow(slotId);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new SlotAlreadyBookedException(slotId);
        }

        slot.setStatus(SlotStatus.BLOCKED);
        slot.setBlockedReason(dto != null ? dto.reason() : null);
        DoctorSlot saved = slotRepository.save(slot);
        log.info("Slot {} blocked", slotId);
        return slotMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public SlotResponseDto unblockSlot(UUID slotId) {
        log.info("Unblocking slot {}", slotId);
        DoctorSlot slot = findSlotOrThrow(slotId);

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new SlotAlreadyBookedException(slotId);
        }

        slot.setStatus(SlotStatus.AVAILABLE);
        slot.setBlockedReason(null);
        DoctorSlot saved = slotRepository.save(slot);
        log.info("Slot {} unblocked", slotId);
        return slotMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public int deleteAvailableSlotsByDoctorAndDate(UUID doctorId, LocalDate date) {
        log.info("Deleting available slots for doctor {} on {}", doctorId, date);
        findDoctorOrThrow(doctorId);
        int deleted = slotRepository.deleteAvailableSlotsByDoctorAndDate(doctorId, date);
        log.info("Deleted {} available slots for doctor {} on {}", deleted, doctorId, date);
        return deleted;
    }

    private List<DoctorSlot> generateDaySlots(Doctor doctor, DoctorSchedule schedule, LocalDate date) {
        List<DoctorSlot> slots = new ArrayList<>();
        LocalTime cursor = schedule.getStartTime();
        int durationMinutes = schedule.getSlotDurationMinutes();
        LocalTime workEnd = schedule.getEndTime();
        LocalTime lunchStart = schedule.getLunchBreakStart();
        LocalTime lunchEnd = schedule.getLunchBreakEnd();

        while (cursor.plusMinutes(durationMinutes).compareTo(workEnd) <= 0) {
            LocalTime slotEnd = cursor.plusMinutes(durationMinutes);

            boolean overlapsLunch = lunchStart != null && lunchEnd != null
                    && cursor.isBefore(lunchEnd) && slotEnd.isAfter(lunchStart);

            if (overlapsLunch) {
                cursor = lunchEnd;
                continue;
            }

            if (!slotRepository.existsByDoctor_IdAndSlotDateAndStartTime(doctor.getId(), date, cursor)) {
                slots.add(DoctorSlot.builder()
                        .doctor(doctor)
                        .schedule(schedule)
                        .slotDate(date)
                        .startTime(cursor)
                        .endTime(slotEnd)
                        .status(SlotStatus.AVAILABLE)
                        .build());
            }

            cursor = slotEnd;
        }

        return slots;
    }

    private DayOfWeek mapToDayOfWeek(java.time.DayOfWeek javaDay) {
        return DayOfWeek.valueOf(javaDay.name());
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new SlotGenerationException("toDate must not be before fromDate");
        }
        if (from.plusDays(MAX_GENERATION_DAYS).isBefore(to)) {
            throw new SlotGenerationException("Date range must not exceed " + MAX_GENERATION_DAYS + " days");
        }
    }

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    log.warn("Doctor not found — ID: {}", doctorId);
                    return new DoctorNotFoundException(doctorId);
                });
    }

    private DoctorSlot findSlotOrThrow(UUID slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> {
                    log.warn("Slot not found — ID: {}", slotId);
                    return new SlotNotFoundException(slotId);
                });
    }
}
