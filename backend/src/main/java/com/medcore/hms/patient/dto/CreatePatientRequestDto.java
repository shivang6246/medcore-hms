package com.medcore.hms.patient.dto;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.patient.entity.BloodGroup;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record CreatePatientRequestDto(

        @NotNull(message = "Hospital ID is required")
        UUID hospitalId,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        Gender gender,

        BloodGroup bloodGroup,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
        @Size(max = 20)
        String phone,

        @Email(message = "Invalid email format")
        @Size(max = 150)
        String email,

        @Valid
        AddressDto address,

        @NotBlank(message = "Emergency contact name is required")
        @Size(max = 150)
        String emergencyContactName,

        @NotBlank(message = "Emergency contact phone is required")
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
