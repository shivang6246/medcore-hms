package com.medcore.hms.prescription.repository;

import com.medcore.hms.prescription.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    Page<Prescription> findByMedicalRecord_Id(UUID medicalRecordId, Pageable pageable);

    List<Prescription> findByMedicalRecord_Id(UUID medicalRecordId);
}
