package com.medcore.hms.doctor.mapper;

import com.medcore.hms.doctor.dto.*;
import com.medcore.hms.doctor.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponseDto toResponseDto(Doctor d) {
        return new DoctorResponseDto(
                d.getId(),
                d.getUser().getId(),
                d.getEmployeeId(),
                d.getUser().getFirstName(),
                d.getUser().getLastName(),
                d.getEmail(),
                d.getUser().getPhone(),
                d.getGender(),
                d.getDateOfBirth(),
                d.getLicenseNumber(),
                d.getSpecialization(),
                d.getQualification(),
                d.getYearsOfExperience(),
                d.getConsultationFee(),
                d.getProfileImageUrl(),
                d.getBiography(),
                d.getIsActive(),
                d.getIsAvailable(),
                new HospitalRefDto(d.getHospital().getId(), d.getHospital().getName()),
                new DepartmentRefDto(d.getDepartment().getId(), d.getDepartment().getName()),
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }

    public DoctorSummaryDto toSummaryDto(Doctor d) {
        return new DoctorSummaryDto(
                d.getId(),
                d.getEmployeeId(),
                d.getUser().getFirstName() + " " + d.getUser().getLastName(),
                d.getEmail(),
                d.getSpecialization(),
                d.getDepartment().getName(),
                d.getHospital().getName(),
                d.getConsultationFee(),
                d.getIsActive()
        );
    }

    public DoctorAvailabilityDto toAvailabilityDto(Doctor d) {
        return new DoctorAvailabilityDto(
                d.getId(),
                null,
                null,
                null,
                d.getIsAvailable()
        );
    }

    public void applyUpdate(UpdateDoctorRequestDto dto, Doctor doctor) {
        if (dto.firstName()         != null) doctor.getUser().setFirstName(dto.firstName());
        if (dto.lastName()          != null) doctor.getUser().setLastName(dto.lastName());
        if (dto.phone()             != null) doctor.getUser().setPhone(dto.phone());
        if (dto.gender()            != null) doctor.setGender(dto.gender());
        if (dto.dateOfBirth()       != null) doctor.setDateOfBirth(dto.dateOfBirth());
        if (dto.specialization()    != null) doctor.setSpecialization(dto.specialization());
        if (dto.qualification()     != null) doctor.setQualification(dto.qualification());
        if (dto.yearsOfExperience() != null) doctor.setYearsOfExperience(dto.yearsOfExperience());
        if (dto.consultationFee()   != null) doctor.setConsultationFee(dto.consultationFee());
        if (dto.profileImageUrl()   != null) doctor.setProfileImageUrl(dto.profileImageUrl());
        if (dto.biography()         != null) doctor.setBiography(dto.biography());
    }
}
