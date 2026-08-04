package com.medcore.hms.ipd.entity;

import com.medcore.hms.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bed",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bed_room_number", columnNames = {"room_id", "bed_number"})
        },
        indexes = {
                @Index(name = "idx_bed_room", columnList = "room_id"),
                @Index(name = "idx_bed_status", columnList = "status"),
                @Index(name = "idx_bed_number", columnList = "bed_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotBlank
    @Column(name = "bed_number", nullable = false, length = 50)
    private String bedNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "daily_rate", precision = 10, scale = 2, nullable = false)
    private BigDecimal dailyRate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
