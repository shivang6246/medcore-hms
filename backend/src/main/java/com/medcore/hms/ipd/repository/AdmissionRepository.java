package com.medcore.hms.ipd.repository;

import com.medcore.hms.ipd.entity.Admission;
import com.medcore.hms.ipd.entity.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, UUID> {

    @Query("SELECT COUNT(a) > 0 FROM Admission a WHERE a.patient.id = :patientId AND a.status IN ('ADMITTED', 'TRANSFERRED')")
    boolean existsActiveAdmissionForPatient(@Param("patientId") UUID patientId);

    Page<Admission> findByPatient_Id(UUID patientId, Pageable pageable);

    Page<Admission> findByStatus(AdmissionStatus status, Pageable pageable);

    Optional<Admission> findByAdmissionNumber(String admissionNumber);
}
