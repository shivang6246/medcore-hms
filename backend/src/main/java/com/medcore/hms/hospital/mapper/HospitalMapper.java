package com.medcore.hms.hospital.mapper;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.common.entity.Address;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import com.medcore.hms.hospital.entity.Hospital;
import org.springframework.stereotype.Component;

@Component
public class HospitalMapper {

    public HospitalResponseDto toResponseDto(Hospital h) {
        return new HospitalResponseDto(
                h.getId(),
                h.getName(),
                h.getRegistrationNumber(),
                h.getLicenseNumber(),
                h.getEmail(),
                h.getPhone(),
                h.getWebsite(),
                h.getDescription(),
                h.getLogoUrl(),
                h.getIsActive(),
                toAddressDto(h.getAddress()),
                h.getCreatedAt(),
                h.getUpdatedAt()
        );
    }

    public HospitalSummaryDto toSummaryDto(Hospital h) {
        return new HospitalSummaryDto(
                h.getId(),
                h.getName(),
                h.getRegistrationNumber(),
                h.getLicenseNumber(),
                h.getEmail(),
                h.getPhone(),
                h.getLogoUrl(),
                h.getIsActive()
        );
    }

    public Hospital toEntity(CreateHospitalRequestDto dto) {
        return Hospital.builder()
                .name(dto.name())
                .registrationNumber(dto.registrationNumber())
                .licenseNumber(dto.licenseNumber())
                .email(dto.email())
                .phone(dto.phone())
                .website(dto.website())
                .description(dto.description())
                .logoUrl(dto.logoUrl())
                .address(toAddressEntity(dto.address()))
                .isActive(true)
                .build();
    }

    public void applyUpdate(UpdateHospitalRequestDto dto, Hospital hospital) {
        if (dto.name()               != null) hospital.setName(dto.name());
        if (dto.registrationNumber() != null) hospital.setRegistrationNumber(dto.registrationNumber());
        if (dto.licenseNumber()      != null) hospital.setLicenseNumber(dto.licenseNumber());
        if (dto.email()              != null) hospital.setEmail(dto.email());
        if (dto.phone()              != null) hospital.setPhone(dto.phone());
        if (dto.website()            != null) hospital.setWebsite(dto.website());
        if (dto.description()        != null) hospital.setDescription(dto.description());
        if (dto.logoUrl()            != null) hospital.setLogoUrl(dto.logoUrl());
        if (dto.address()            != null) {
            if (hospital.getAddress() == null) {
                hospital.setAddress(toAddressEntity(dto.address()));
            } else {
                updateAddressEntity(dto.address(), hospital.getAddress());
            }
        }
    }

    public AddressDto toAddressDto(Address address) {
        if (address == null) return null;
        return new AddressDto(
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }

    public Address toAddressEntity(AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .street(dto.street())
                .city(dto.city())
                .state(dto.state())
                .postalCode(dto.postalCode())
                .country(dto.country())
                .build();
    }

    public void updateAddressEntity(AddressDto dto, Address address) {
        if (dto == null || address == null) return;
        if (dto.street()     != null) address.setStreet(dto.street());
        if (dto.city()       != null) address.setCity(dto.city());
        if (dto.state()      != null) address.setState(dto.state());
        if (dto.postalCode() != null) address.setPostalCode(dto.postalCode());
        if (dto.country()    != null) address.setCountry(dto.country());
    }
}
