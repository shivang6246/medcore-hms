package com.medcore.hms.ipd.entity;

import com.medcore.hms.billing.entity.Invoice;
import com.medcore.hms.common.entity.BaseEntity;
import com.medcore.hms.doctor.entity.Doctor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "discharge_summary",
        indexes = {
                @Index(name = "idx_discharge_admission", columnList = "admission_id"),
                @Index(name = "idx_discharge_doctor", columnList = "attending_doctor_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DischargeSummary extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_id", nullable = false)
    private Admission admission;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attending_doctor_id", nullable = false)
    private Doctor attendingDoctor;

    @Column(name = "discharge_date", nullable = false)
    private LocalDateTime dischargeDate;

    @Column(name = "final_diagnosis", columnDefinition = "TEXT")
    private String finalDiagnosis;

    @Column(name = "treatment_summary", columnDefinition = "TEXT")
    private String treatmentSummary;

    @Column(name = "discharge_notes", columnDefinition = "TEXT")
    private String dischargeNotes;

    @Column(name = "follow_up_instructions", columnDefinition = "TEXT")
    private String followUpInstructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_invoice_id")
    private Invoice finalInvoice;
}
