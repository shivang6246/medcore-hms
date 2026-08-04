package com.medcore.hms.pharmacy.repository;

import com.medcore.hms.pharmacy.entity.DispenseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DispenseRecordRepository extends JpaRepository<DispenseRecord, UUID> {

    Page<DispenseRecord> findByPatient_Id(UUID patientId, Pageable pageable);

    boolean existsByDispenseNumber(String dispenseNumber);
}
