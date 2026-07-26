package com.medcore.hms.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Represents a physical postal address.
 * Standalone entity — currently linked 1:1 to Patient,
 * but designed to be reusable for Hospital and Doctor in future iterations.
 */
@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @NotBlank(message = "Street is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String state;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20)
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false)
    private String country;
}
