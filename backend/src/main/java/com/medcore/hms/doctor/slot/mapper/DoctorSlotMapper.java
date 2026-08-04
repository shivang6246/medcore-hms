package com.medcore.hms.doctor.slot.mapper;

import com.medcore.hms.doctor.slot.dto.SlotResponseDto;
import com.medcore.hms.doctor.slot.dto.SlotSummaryDto;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import org.springframework.stereotype.Component;

@Component
public class DoctorSlotMapper {

    public SlotResponseDto toResponseDto(DoctorSlot s) {
        return new SlotResponseDto(
                s.getId(),
                s.getDoctor().getId(),
                s.getSchedule() != null ? s.getSchedule().getId() : null,
                s.getSlotDate(),
                s.getStartTime(),
                s.getEndTime(),
                s.getStatus(),
                s.getBlockedReason(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    public SlotSummaryDto toSummaryDto(DoctorSlot s) {
        return new SlotSummaryDto(
                s.getId(),
                s.getSlotDate(),
                s.getStartTime(),
                s.getEndTime(),
                s.getStatus()
        );
    }
}
