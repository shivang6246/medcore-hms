package com.medcore.hms.lab.entity;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * LabTest entity representing a laboratory order for a patient.
 */
@Entity
@Table(
        name = "lab_test",
        indexes = {
                @Index(name = "idx_labtest_patient",    columnList = "patient_id"),
                @Index(name = "idx_labtest_doctor",     columnList = "doctor_id"),
                @Index(name = "idx_labtest_status",     columnList = "status"),
                @Index(name = "idx_labtest_priority",   columnList = "priority"),
                @Index(name = "idx_labtest_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTest extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    @NotBlank
    @Column(name = "test_type", nullable = false, length = 150)
    private String testType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TestPriority priority = TestPriority.NORMAL;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private LabTestStatus status = LabTestStatus.REQUESTED;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToOne(mappedBy = "labTest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LabReport labReport;
}
