package com.medcore.hms.ipd.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admission",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admission_number", columnNames = "admission_number")
        },
        indexes = {
                @Index(name = "idx_admission_number", columnList = "admission_number"),
                @Index(name = "idx_admission_patient", columnList = "patient_id"),
                @Index(name = "idx_admission_doctor", columnList = "doctor_id"),
                @Index(name = "idx_admission_bed", columnList = "bed_id"),
                @Index(name = "idx_admission_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admission extends BaseEntity {

    @Column(name = "admission_number", nullable = false, unique = true, length = 30)
    private String admissionNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @NotNull
    @Column(name = "admission_date", nullable = false)
    private LocalDateTime admissionDate;

    @Column(name = "expected_discharge_date")
    private LocalDateTime expectedDischargeDate;

    @Column(name = "discharge_date")
    private LocalDateTime dischargeDate;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private AdmissionStatus status = AdmissionStatus.ADMITTED;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToOne(mappedBy = "admission", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DischargeSummary dischargeSummary;
}
