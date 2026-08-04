package com.medcore.hms.patient.entity;

import com.medcore.hms.common.entity.Address;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.hospital.entity.Hospital;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "patient",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_hospital_pid",   columnNames = {"hospital_id", "patient_id"}),
                @UniqueConstraint(name = "uk_patient_hospital_phone", columnNames = {"hospital_id", "phone"}),
                @UniqueConstraint(name = "uk_patient_email",          columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_patient_hospital_id",     columnList = "hospital_id"),
                @Index(name = "idx_patient_hospital_active", columnList = "hospital_id, is_active"),
                @Index(name = "idx_patient_patient_id",      columnList = "patient_id"),
                @Index(name = "idx_patient_phone",           columnList = "phone"),
                @Index(name = "idx_patient_email",           columnList = "email"),
                @Index(name = "idx_patient_blood_group",     columnList = "blood_group"),
                @Index(name = "idx_patient_dob",             columnList = "date_of_birth"),
                @Index(name = "idx_patient_name",            columnList = "last_name, first_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank
    @Size(max = 20)
    @Column(name = "patient_id", nullable = false, length = 20)
    private String patientId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 20)
    private BloodGroup bloodGroup;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid phone number format")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String phone;

    @Email
    @Size(max = 150)
    @Column(unique = true, length = 150)
    private String email;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    @NotBlank
    @Size(max = 150)
    @Column(name = "emergency_contact_name", nullable = false, length = 150)
    private String emergencyContactName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\-\\s]{7,20}$", message = "Invalid emergency contact phone format")
    @Size(max = 20)
    @Column(name = "emergency_contact_phone", nullable = false, length = 20)
    private String emergencyContactPhone;

    @Size(max = 50)
    @Column(name = "emergency_contact_relationship", length = 50)
    private String emergencyContactRelationship;

    @Size(max = 150)
    @Column(name = "insurance_provider", length = 150)
    private String insuranceProvider;

    @Size(max = 100)
    @Column(name = "insurance_policy_number", length = 100)
    private String insurancePolicyNumber;

    @Size(max = 5000)
    @Column(columnDefinition = "TEXT")
    private String allergies;

    @Size(max = 10000)
    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // TODO: Appointment  — @OneToMany(mappedBy = "patient") List<Appointment> appointments
    // TODO: MedicalRecord — @OneToMany(mappedBy = "patient") List<MedicalRecord> medicalRecords
    // TODO: Prescription  — @OneToMany(mappedBy = "patient") List<Prescription> prescriptions
    // TODO: Bill          — @OneToMany(mappedBy = "patient") List<Bill> bills
    // TODO: LabReport     — @OneToMany(mappedBy = "patient") List<LabReport> labReports
}
