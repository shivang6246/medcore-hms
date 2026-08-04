package com.medcore.hms.ipd.entity;

import com.medcore.hms.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_ward_number", columnNames = {"ward_id", "room_number"})
        },
        indexes = {
                @Index(name = "idx_room_ward", columnList = "ward_id"),
                @Index(name = "idx_room_number", columnList = "room_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @NotBlank
    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @Column(name = "room_type", length = 50)
    private String roomType; // Single, Double, Suite, Deluxe

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bed> beds = new ArrayList<>();
}
