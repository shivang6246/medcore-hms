package com.medcore.hms.pharmacy.entity;

import com.medcore.hms.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Supplier entity representing pharmaceutical vendors and distributors.
 */
@Entity
@Table(
        name = "supplier",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_supplier_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_supplier_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_supplier_name", columnList = "name"),
                @Index(name = "idx_supplier_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
