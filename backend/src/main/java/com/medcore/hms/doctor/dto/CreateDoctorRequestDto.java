package com.medcore.hms.doctor.dto;

import com.medcore.hms.doctor.entity.Gender;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateDoctorRequestDto(

                @NotBlank(message = "First name is required") @Size(max = 100) String firstName,

                @NotBlank(message = "Last name is required") @Size(max = 100) String lastName,

                @NotBlank(message = "Email is required") @Email(message = "Invalid email format") @Size(max = 150) String email,

                @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters") String password,

                @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format") @Size(max = 20) String phone,

                @NotBlank(message = "Employee ID is required") @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9\\-]{3,50}$", message = "Employee ID must be 3–50 alphanumeric characters or hyphens") String employeeId,

                Gender gender,

                @Past(message = "Date of birth must be in the past") LocalDate dateOfBirth,

                UUID hospitalId,

                @NotNull(message = "Department is required") UUID departmentId,

                @NotBlank(message = "License number is required") @Size(max = 100) @Pattern(regexp = "^[A-Za-z0-9\\-]{5,100}$", message = "License number must be 5–100 alphanumeric characters or hyphens") String licenseNumber,

                @NotBlank(message = "Specialization is required") @Size(max = 150) String specialization,

                @Size(max = 255) String qualification,

                @Min(value = 0, message = "Years of experience cannot be negative") @Max(value = 60, message = "Years of experience cannot exceed 60") Integer yearsOfExperience,

                @DecimalMin(value = "0.00", message = "Consultation fee cannot be negative") @Digits(integer = 8, fraction = 2, message = "Consultation fee must have up to 8 digits and 2 decimal places") BigDecimal consultationFee,

                @Size(max = 500) @Pattern(regexp = "^(https?://.*)?$", message = "Profile image URL must be a valid http or https URL") String profileImageUrl,

                @Size(max = 5000) String biography) {
}
