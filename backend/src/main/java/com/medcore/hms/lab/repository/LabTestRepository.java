package com.medcore.hms.lab.repository;

import com.medcore.hms.lab.entity.LabTest;
import com.medcore.hms.lab.entity.LabTestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, UUID> {

    Page<LabTest> findByPatient_Id(UUID patientId, Pageable pageable);

    Page<LabTest> findByDoctor_Id(UUID doctorId, Pageable pageable);

    Page<LabTest> findByStatus(LabTestStatus status, Pageable pageable);
}
