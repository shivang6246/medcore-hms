package com.medcore.hms.pharmacy.entity;

import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.prescription.entity.Prescription;
import com.medcore.hms.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "dispense_record",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dispense_number", columnNames = "dispense_number")
        },
        indexes = {
                @Index(name = "idx_dispense_patient", columnList = "patient_id"),
                @Index(name = "idx_dispense_pharmacist", columnList = "pharmacist_id"),
                @Index(name = "idx_dispense_date", columnList = "dispensed_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispenseRecord extends BaseEntity {

    @Column(name = "dispense_number", nullable = false, unique = true, length = 30)
    private String dispenseNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private User pharmacist;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "dispensed_at", nullable = false)
    private LocalDateTime dispensedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "dispenseRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DispenseItem> items = new ArrayList<>();
}
