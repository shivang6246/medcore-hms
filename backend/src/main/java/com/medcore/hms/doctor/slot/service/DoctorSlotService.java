package com.medcore.hms.doctor.slot.service;

import com.medcore.hms.doctor.slot.dto.BlockSlotRequestDto;
import com.medcore.hms.doctor.slot.dto.GenerateSlotsRequestDto;
import com.medcore.hms.doctor.slot.dto.SlotResponseDto;
import com.medcore.hms.doctor.slot.dto.SlotSummaryDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DoctorSlotService {

    List<SlotSummaryDto> generateSlots(UUID doctorId, GenerateSlotsRequestDto dto);

    List<SlotSummaryDto> getSlotsByDoctorAndDate(UUID doctorId, LocalDate date);

    List<SlotSummaryDto> getSlotsByDoctorAndDateRange(UUID doctorId, LocalDate from, LocalDate to);

    SlotResponseDto getSlotById(UUID slotId);

    SlotResponseDto blockSlot(UUID slotId, BlockSlotRequestDto dto);

    SlotResponseDto unblockSlot(UUID slotId);

    int deleteAvailableSlotsByDoctorAndDate(UUID doctorId, LocalDate date);
}
