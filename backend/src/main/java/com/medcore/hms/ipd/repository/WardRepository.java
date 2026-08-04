package com.medcore.hms.ipd.repository;

import com.medcore.hms.ipd.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WardRepository extends JpaRepository<Ward, UUID> {
    boolean existsByName(String name);
}
