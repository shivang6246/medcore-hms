package com.medcore.hms.doctor.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Doctor profile linked 1:1 to a User identity.
 * Scoped to exactly one Hospital and one Department.
 */
@Entity
@Table(
        name = "doctor",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_user",        columnNames = "user_id"),
                @UniqueConstraint(name = "uk_doctor_license",     columnNames = "license_number"),
                @UniqueConstraint(name = "uk_doctor_email",       columnNames = "email"),
                @UniqueConstraint(name = "uk_doctor_hospital_emp", columnNames = {"hospital_id", "employee_id"})
        },
        indexes = {
                @Index(name = "idx_doctor_hospital_id",     columnList = "hospital_id"),
                @Index(name = "idx_doctor_department_id",   columnList = "department_id"),
                @Index(name = "idx_doctor_license",         columnList = "license_number"),
                @Index(name = "idx_doctor_email",           columnList = "email"),
                @Index(name = "idx_doctor_specialization",  columnList = "specialization"),
                @Index(name = "idx_doctor_active",          columnList = "is_active"),
                @Index(name = "idx_doctor_active_hospital", columnList = "hospital_id, is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotBlank
    @Size(max = 50)
    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Past
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(max = 100)
    @Column(name = "license_number", nullable = false, unique = true, length = 100)
    private String licenseNumber;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String specialization;

    @Size(max = 255)
    @Column(length = 255)
    private String qualification;

    @Min(0) @Max(60)
    @Column(name = "years_of_experience")
    @Builder.Default
    private Integer yearsOfExperience = 0;

    @DecimalMin("0.00")
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Size(max = 500)
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // TODO: Appointment — @OneToMany(mappedBy = "doctor") List<Appointment> appointments
    // TODO: Prescription — @OneToMany(mappedBy = "doctor") List<Prescription> prescriptions
}
