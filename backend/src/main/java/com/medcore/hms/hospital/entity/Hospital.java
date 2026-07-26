package com.medcore.hms.hospital.entity;

import com.medcore.hms.common.entity.Address;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Root tenant entity. Every hospital is a completely isolated tenant.
 * All tenant-scoped entities carry a hospital_id FK for multi-tenancy enforcement.
 * SUPER_ADMIN can see all hospitals; all other roles are scoped to their own hospital.
 */
@Entity
@Table(
        name = "hospital",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hospital_reg_num", columnNames = "registration_number"),
                @UniqueConstraint(name = "uk_hospital_license_num", columnNames = "license_number"),
                @UniqueConstraint(name = "uk_hospital_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_hospital_registration", columnList = "registration_number"),
                @Index(name = "idx_hospital_license", columnList = "license_number"),
                @Index(name = "idx_hospital_email", columnList = "email"),
                @Index(name = "idx_hospital_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital extends BaseEntity {

    @NotBlank(message = "Hospital name is required")
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Registration number is required")
    @Size(max = 100)
    @Column(name = "registration_number", nullable = false, unique = true, length = 100)
    private String registrationNumber;

    @NotBlank(message = "License number is required")
    @Size(max = 100)
    @Column(name = "license_number", nullable = false, unique = true, length = 100, columnDefinition = "VARCHAR(100) DEFAULT 'LIC-TEMP'")
    private String licenseNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    @Size(max = 20)
    @Column(length = 20)
    private String phone;

    @Size(max = 255)
    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Size(max = 255)
    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    // ---- Relationships ----

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Department> departments = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "hospital", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Patient> patients = new ArrayList<>();
}
