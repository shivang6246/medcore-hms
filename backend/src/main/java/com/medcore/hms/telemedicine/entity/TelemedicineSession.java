package com.medcore.hms.telemedicine.entity;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "telemedicine_session",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_telem_room_code", columnNames = "room_code"),
                @UniqueConstraint(name = "uk_telem_appointment", columnNames = "appointment_id")
        },
        indexes = {
                @Index(name = "idx_telem_room_code", columnList = "room_code"),
                @Index(name = "idx_telem_doctor", columnList = "doctor_id"),
                @Index(name = "idx_telem_patient", columnList = "patient_id"),
                @Index(name = "idx_telem_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemedicineSession extends BaseEntity {

    @NotBlank
    @Column(name = "room_code", nullable = false, unique = true, length = 100)
    private String roomCode;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @Column(name = "scheduled_start_time", nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ConsultationSessionStatus status = ConsultationSessionStatus.WAITING_ROOM;

    @Column(name = "doctor_token", length = 200)
    private String doctorToken;

    @Column(name = "patient_token", length = 200)
    private String patientToken;

    @Column(columnDefinition = "TEXT")
    private String summaryNotes;
}
