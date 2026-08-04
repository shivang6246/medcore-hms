package com.medcore.hms.patient.mapper;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.common.entity.Address;
import com.medcore.hms.doctor.dto.HospitalRefDto;
import com.medcore.hms.patient.dto.*;
import com.medcore.hms.patient.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponseDto toResponseDto(Patient p) {
        return new PatientResponseDto(
                p.getId(),
                p.getPatientId(),
                p.getFirstName(),
                p.getLastName(),
                p.getDateOfBirth(),
                p.getGender(),
                p.getBloodGroup(),
                p.getPhone(),
                p.getEmail(),
                toAddressDto(p.getAddress()),
                new EmergencyContactDto(
                        p.getEmergencyContactName(),
                        p.getEmergencyContactPhone(),
                        p.getEmergencyContactRelationship()
                ),
                p.getInsuranceProvider(),
                p.getInsurancePolicyNumber(),
                p.getAllergies(),
                p.getMedicalHistory(),
                p.getIsActive(),
                new HospitalRefDto(p.getHospital().getId(), p.getHospital().getName()),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    public PatientSummaryDto toSummaryDto(Patient p) {
        return new PatientSummaryDto(
                p.getId(),
                p.getPatientId(),
                p.getFirstName(),
                p.getLastName(),
                p.getDateOfBirth(),
                p.getPhone(),
                p.getEmail(),
                p.getBloodGroup(),
                p.getIsActive(),
                p.getHospital().getName()
        );
    }

    public void applyUpdate(UpdatePatientRequestDto dto, Patient patient) {
        if (dto.firstName()                    != null) patient.setFirstName(dto.firstName());
        if (dto.lastName()                     != null) patient.setLastName(dto.lastName());
        if (dto.dateOfBirth()                  != null) patient.setDateOfBirth(dto.dateOfBirth());
        if (dto.gender()                       != null) patient.setGender(dto.gender());
        if (dto.bloodGroup()                   != null) patient.setBloodGroup(dto.bloodGroup());
        if (dto.phone()                        != null) patient.setPhone(dto.phone());
        if (dto.email()                        != null) patient.setEmail(dto.email());
        if (dto.emergencyContactName()         != null) patient.setEmergencyContactName(dto.emergencyContactName());
        if (dto.emergencyContactPhone()        != null) patient.setEmergencyContactPhone(dto.emergencyContactPhone());
        if (dto.emergencyContactRelationship() != null) patient.setEmergencyContactRelationship(dto.emergencyContactRelationship());
        if (dto.insuranceProvider()            != null) patient.setInsuranceProvider(dto.insuranceProvider());
        if (dto.insurancePolicyNumber()        != null) patient.setInsurancePolicyNumber(dto.insurancePolicyNumber());
        if (dto.allergies()                    != null) patient.setAllergies(dto.allergies());
        if (dto.medicalHistory()               != null) patient.setMedicalHistory(dto.medicalHistory());
        if (dto.address() != null) {
            if (patient.getAddress() == null) {
                patient.setAddress(toAddressEntity(dto.address()));
            } else {
                applyAddressUpdate(dto.address(), patient.getAddress());
            }
        }
    }

    private AddressDto toAddressDto(Address a) {
        if (a == null) return null;
        return new AddressDto(a.getStreet(), a.getCity(), a.getState(), a.getPostalCode(), a.getCountry());
    }

    private Address toAddressEntity(AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .street(dto.street())
                .city(dto.city())
                .state(dto.state())
                .postalCode(dto.postalCode())
                .country(dto.country())
                .build();
    }

    private void applyAddressUpdate(AddressDto dto, Address address) {
        if (dto.street()     != null) address.setStreet(dto.street());
        if (dto.city()       != null) address.setCity(dto.city());
        if (dto.state()      != null) address.setState(dto.state());
        if (dto.postalCode() != null) address.setPostalCode(dto.postalCode());
        if (dto.country()    != null) address.setCountry(dto.country());
    }
}
