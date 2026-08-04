package com.medcore.hms.doctor.schedule.mapper;

import com.medcore.hms.doctor.schedule.dto.ScheduleResponseDto;
import com.medcore.hms.doctor.schedule.dto.ScheduleSummaryDto;
import com.medcore.hms.doctor.schedule.dto.UpdateScheduleRequestDto;
import com.medcore.hms.doctor.schedule.entity.DoctorSchedule;
import org.springframework.stereotype.Component;

@Component
public class DoctorScheduleMapper {

    public ScheduleResponseDto toResponseDto(DoctorSchedule s) {
        return new ScheduleResponseDto(
                s.getId(),
                s.getDoctor().getId(),
                s.getDoctor().getUser().getFirstName() + " " + s.getDoctor().getUser().getLastName(),
                s.getDayOfWeek(),
                s.getStartTime(),
                s.getEndTime(),
                s.getLunchBreakStart(),
                s.getLunchBreakEnd(),
                s.getSlotDurationMinutes(),
                s.getIsActive(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public ScheduleSummaryDto toSummaryDto(DoctorSchedule s) {
        return new ScheduleSummaryDto(
                s.getId(),
                s.getDoctor().getId(),
                s.getDayOfWeek(),
                s.getStartTime(),
                s.getEndTime(),
                s.getSlotDurationMinutes(),
                s.getIsActive()
        );
    }

    public void applyUpdate(UpdateScheduleRequestDto dto, DoctorSchedule schedule) {
        if (dto.dayOfWeek()           != null) schedule.setDayOfWeek(dto.dayOfWeek());
        if (dto.startTime()           != null) schedule.setStartTime(dto.startTime());
        if (dto.endTime()             != null) schedule.setEndTime(dto.endTime());
        if (dto.lunchBreakStart()     != null) schedule.setLunchBreakStart(dto.lunchBreakStart());
        if (dto.lunchBreakEnd()       != null) schedule.setLunchBreakEnd(dto.lunchBreakEnd());
        if (dto.slotDurationMinutes() != null) schedule.setSlotDurationMinutes(dto.slotDurationMinutes());
    }
}
