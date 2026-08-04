package com.medcore.hms.pharmacy.entity;

import com.medcore.hms.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicine master catalog entity storing drug definitions, pricing, category, reorder thresholds, and total stock.
 */
@Entity
@Table(
        name = "medicine",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medicine_name_brand", columnNames = {"name", "brand"}),
                @UniqueConstraint(name = "uk_medicine_barcode", columnNames = "barcode")
        },
        indexes = {
                @Index(name = "idx_medicine_name", columnList = "name"),
                @Index(name = "idx_medicine_category", columnList = "category"),
                @Index(name = "idx_medicine_barcode", columnList = "barcode"),
                @Index(name = "idx_medicine_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "generic_name", length = 150)
    private String genericName;

    @Column(length = 100)
    private String brand;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String category;

    @Column(length = 100)
    private String strength;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(length = 150)
    private String manufacturer;

    @Column(length = 100)
    private String barcode;

    @NotNull
    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @NotNull
    @Min(0)
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicineBatch> batches = new ArrayList<>();
}
