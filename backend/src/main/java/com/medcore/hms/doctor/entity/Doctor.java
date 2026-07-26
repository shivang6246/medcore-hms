package com.medcore.hms.doctor.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Doctor profile extending User (1:1 via user_id).
 * Stores professional information: license, specialization, fee, availability.
 * A doctor belongs to one department within one hospital.
 */
@Entity
@Table(name = "doctor",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "license_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    /** 1:1 link to the User identity record. */
    @NotNull(message = "User reference is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Hospital is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull(message = "Department is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotBlank(message = "License number is required")
    @Size(max = 100)
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @NotBlank(message = "Specialization is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String specialization;

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 60, message = "Experience years cannot exceed 60")
    @Column(name = "experience_years")
    private Integer experienceYears;

    @Size(max = 255)
    private String qualification;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}
