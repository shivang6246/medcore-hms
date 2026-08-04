package com.medcore.hms.pharmacy.entity;

import com.medcore.hms.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * MedicineBatch entity tracking specific stock batches with expiry date and purchase/selling prices.
 */
@Entity
@Table(
        name = "medicine_batch",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_batch_medicine_number", columnNames = {"medicine_id", "batch_number"})
        },
        indexes = {
                @Index(name = "idx_batch_medicine", columnList = "medicine_id"),
                @Index(name = "idx_batch_expiry", columnList = "expiry_date"),
                @Index(name = "idx_batch_number", columnList = "batch_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicineBatch extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @NotBlank
    @Column(name = "batch_number", nullable = false, length = 100)
    private String batchNumber;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "purchase_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal purchasePrice;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "selling_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal sellingPrice;

    @NotNull
    @Min(0)
    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @NotNull
    @Min(0)
    @Column(name = "current_quantity", nullable = false)
    private Integer currentQuantity;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
