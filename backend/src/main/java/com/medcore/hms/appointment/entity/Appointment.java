package com.medcore.hms.appointment.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.patient.entity.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_appointment_slot",   columnNames = "slot_id"),
                @UniqueConstraint(name = "uk_appointment_number", columnNames = "appointment_number")
        },
        indexes = {
                @Index(name = "idx_appt_hospital",      columnList = "hospital_id"),
                @Index(name = "idx_appt_doctor_date",   columnList = "doctor_id, appointment_date"),
                @Index(name = "idx_appt_patient",       columnList = "patient_id"),
                @Index(name = "idx_appt_status",        columnList = "status"),
                @Index(name = "idx_appt_date",          columnList = "appointment_date"),
                @Index(name = "idx_appt_hospital_status", columnList = "hospital_id, status"),
                @Index(name = "idx_appt_doctor_status", columnList = "doctor_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment extends BaseEntity {

    @NotBlank
    @Size(max = 30)
    @Column(name = "appointment_number", nullable = false, unique = true, length = 30)
    private String appointmentNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

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
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private DoctorSlot slot;

    @NotNull
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private AppointmentType type = AppointmentType.IN_PERSON;

    @Size(max = 500)
    @Column(name = "chief_complaint", length = 500)
    private String chiefComplaint;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Size(max = 500)
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @DecimalMin("0.00")
    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @OneToOne(mappedBy = "appointment", fetch = FetchType.LAZY)
    private com.medcore.hms.medicalrecord.entity.MedicalRecord medicalRecord;

    // TODO: Prescription  — @OneToMany(mappedBy = "appointment") List<Prescription> prescriptions
    // TODO: Bill          — @OneToOne(mappedBy = "appointment") Bill bill
}
