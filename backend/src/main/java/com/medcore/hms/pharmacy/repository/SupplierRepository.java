package com.medcore.hms.pharmacy.repository;

import com.medcore.hms.pharmacy.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    boolean existsByName(String name);
}
