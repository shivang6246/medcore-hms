package com.medcore.hms.prescription.mapper;

import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.prescription.dto.CreatePrescriptionRequestDto;
import com.medcore.hms.prescription.dto.PrescriptionResponseDto;
import com.medcore.hms.prescription.dto.PrescriptionSummaryDto;
import com.medcore.hms.prescription.dto.UpdatePrescriptionRequestDto;
import com.medcore.hms.prescription.entity.Prescription;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {

    public PrescriptionResponseDto toResponseDto(Prescription p) {
        if (p == null) return null;
        return new PrescriptionResponseDto(
                p.getId(),
                p.getMedicalRecord() != null ? p.getMedicalRecord().getId() : null,
                p.getMedicineName(),
                p.getDosage(),
                p.getFrequency(),
                p.getDuration(),
                p.getInstructions(),
                p.getQuantity(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public PrescriptionSummaryDto toSummaryDto(Prescription p) {
        if (p == null) return null;
        return new PrescriptionSummaryDto(
                p.getId(),
                p.getMedicineName(),
                p.getDosage(),
                p.getFrequency(),
                p.getDuration(),
                p.isActive()
        );
    }

    public Prescription toEntity(CreatePrescriptionRequestDto dto, MedicalRecord medicalRecord) {
        return Prescription.builder()
                .medicalRecord(medicalRecord)
                .medicineName(dto.medicineName())
                .dosage(dto.dosage())
                .frequency(dto.frequency())
                .duration(dto.duration())
                .instructions(dto.instructions())
                .quantity(dto.quantity())
                .isActive(true)
                .build();
    }

    public void updateEntity(Prescription entity, UpdatePrescriptionRequestDto dto) {
        if (dto.medicineName() != null) entity.setMedicineName(dto.medicineName());
        if (dto.dosage() != null) entity.setDosage(dto.dosage());
        if (dto.frequency() != null) entity.setFrequency(dto.frequency());
        if (dto.duration() != null) entity.setDuration(dto.duration());
        if (dto.instructions() != null) entity.setInstructions(dto.instructions());
        if (dto.quantity() != null) entity.setQuantity(dto.quantity());
    }
}
