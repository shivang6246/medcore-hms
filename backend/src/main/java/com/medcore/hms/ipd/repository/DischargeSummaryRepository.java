package com.medcore.hms.ipd.repository;

import com.medcore.hms.ipd.entity.DischargeSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DischargeSummaryRepository extends JpaRepository<DischargeSummary, UUID> {
    Optional<DischargeSummary> findByAdmission_Id(UUID admissionId);
}
