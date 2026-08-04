package com.medcore.hms.patient.dto;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.patient.entity.BloodGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UpdatePatientRequestDto(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,

        BloodGroup bloodGroup,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
        @Size(max = 20)
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 150)
        String email,

        @Valid
        AddressDto address,

        @Size(max = 150)
        String emergencyContactName,

        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid emergency contact phone format")
        @Size(max = 20)
        String emergencyContactPhone,

        @Size(max = 50)
        String emergencyContactRelationship,

        @Size(max = 150)
        String insuranceProvider,

        @Size(max = 100)
        String insurancePolicyNumber,

        @Size(max = 5000)
        String allergies,

        @Size(max = 10000)
        String medicalHistory
) {}
