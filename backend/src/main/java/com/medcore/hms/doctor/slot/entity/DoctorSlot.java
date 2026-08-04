package com.medcore.hms.doctor.slot.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.schedule.entity.DoctorSchedule;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "doctor_slot",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_slot_doctor_date_start", columnNames = {"doctor_id", "slot_date", "start_time"})
        },
        indexes = {
                @Index(name = "idx_slot_doctor_date", columnList = "doctor_id, slot_date"),
                @Index(name = "idx_slot_status", columnList = "status"),
                @Index(name = "idx_slot_doctor_status", columnList = "doctor_id, status"),
                @Index(name = "idx_slot_date_range", columnList = "doctor_id, slot_date, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSlot extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private DoctorSchedule schedule;

    @NotNull
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

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
    private SlotStatus status = SlotStatus.AVAILABLE;

    @Column(name = "blocked_reason", length = 255)
    private String blockedReason;
}
