package com.medcore.hms.patient.repository;

import com.medcore.hms.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByUser_Id(UUID userId);

    List<Patient> findByHospital_Id(UUID hospitalId);

    /** Returns only non-deleted patients for a given hospital. */
    @Query("SELECT p FROM Patient p WHERE p.hospital.id = :hospitalId AND p.deletedAt IS NULL")
    List<Patient> findActiveByHospitalId(@Param("hospitalId") UUID hospitalId);
}
