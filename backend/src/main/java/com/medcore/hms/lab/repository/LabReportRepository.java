package com.medcore.hms.lab.repository;

import com.medcore.hms.lab.entity.LabReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, UUID> {

    Optional<LabReport> findByLabTest_Id(UUID labTestId);
}
