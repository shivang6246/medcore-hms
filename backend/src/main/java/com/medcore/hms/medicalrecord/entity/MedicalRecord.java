package com.medcore.hms.medicalrecord.entity;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Medical Record entity representing clinical notes, diagnosis, symptoms, and treatment plan for an appointment.
 * 
 * <p>Future extension points:
 * <ul>
 *   <li>Prescription integration (@OneToMany)</li>
 *   <li>Lab Reports (@OneToMany)</li>
 *   <li>Clinical Attachments / Documents (@ElementCollection or @OneToMany)</li>
 *   <li>Patient Vitals (@Embedded or @OneToOne)</li>
 * </ul>
 */
@Entity
@Table(
        name = "medical_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medical_record_appointment", columnNames = "appointment_id")
        },
        indexes = {
                @Index(name = "idx_medrec_patient",      columnList = "patient_id"),
                @Index(name = "idx_medrec_doctor",       columnList = "doctor_id"),
                @Index(name = "idx_medrec_appointment",  columnList = "appointment_id"),
                @Index(name = "idx_medrec_active",       columnList = "is_active"),
                @Index(name = "idx_medrec_created_at",   columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String symptoms;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String diagnosis;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<com.medcore.hms.prescription.entity.Prescription> prescriptions = new java.util.ArrayList<>();
}
