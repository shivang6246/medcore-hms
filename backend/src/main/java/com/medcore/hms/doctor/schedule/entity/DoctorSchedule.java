package com.medcore.hms.doctor.schedule.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "doctor_schedule",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_doctor_day", columnNames = {"doctor_id", "day_of_week"})
        },
        indexes = {
                @Index(name = "idx_schedule_doctor_id", columnList = "doctor_id"),
                @Index(name = "idx_schedule_doctor_active", columnList = "doctor_id, is_active"),
                @Index(name = "idx_schedule_day", columnList = "day_of_week")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSchedule extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "lunch_break_start")
    private LocalTime lunchBreakStart;

    @Column(name = "lunch_break_end")
    private LocalTime lunchBreakEnd;

    @NotNull
    @Min(5) @Max(120)
    @Column(name = "slot_duration_minutes", nullable = false)
    @Builder.Default
    private Integer slotDurationMinutes = 30;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
