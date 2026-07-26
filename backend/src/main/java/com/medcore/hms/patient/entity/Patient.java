package com.medcore.hms.patient.entity;

import com.medcore.hms.common.entity.Address;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Patient profile extending User (1:1 via user_id).
 * Stores medical information: DOB, blood group, emergency contact, medical history.
 * Supports soft-delete — patient records are never hard-deleted (compliance requirement).
 */
@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    /** 1:1 link to the User identity record. */
    @NotNull(message = "User reference is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull(message = "Hospital is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    /** Patient's home address — cascaded (created/deleted with patient). */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 10)
    @Column(length = 10)
    private String gender;

    @Size(max = 10)
    @Column(name = "blood_group", length = 10)
    private String bloodGroup;

    @Size(max = 150)
    @Column(name = "emergency_contact_name", length = 150)
    private String emergencyContactName;

    @Size(max = 20)
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    /** Soft-delete timestamp. When set, patient is considered deleted. */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
