package com.medcore.hms.prescription.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Prescription entity representing prescribed medicine, dosage, frequency, and instructions for a medical record visit.
 */
@Entity
@Table(
        name = "prescription",
        indexes = {
                @Index(name = "idx_prescription_medrec", columnList = "medical_record_id"),
                @Index(name = "idx_prescription_medicine", columnList = "medicine_name"),
                @Index(name = "idx_prescription_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @NotBlank
    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String dosage;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String frequency;

    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Min(1)
    @Column
    private Integer quantity;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
