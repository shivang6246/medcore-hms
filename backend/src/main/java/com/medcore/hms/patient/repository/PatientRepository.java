package com.medcore.hms.patient.repository;

import com.medcore.hms.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"hospital", "address"})
    Optional<Patient> findById(@NonNull UUID id);

    Optional<Patient> findByPatientIdAndHospital_Id(String patientId, UUID hospitalId);

    Optional<Patient> findByEmail(String email);

    Optional<Patient> findByPhoneAndHospital_Id(String phone, UUID hospitalId);

    Page<Patient> findByHospital_Id(UUID hospitalId, Pageable pageable);

    Page<Patient> findByHospital_IdAndIsActiveTrue(UUID hospitalId, Pageable pageable);

    List<Patient> findByHospital_IdAndIsActiveTrue(UUID hospitalId);

    long countByGender(com.medcore.hms.doctor.entity.Gender gender);

    long countByHospital_Id(UUID hospitalId);

    boolean existsByPatientIdAndHospital_Id(String patientId, UUID hospitalId);

    boolean existsByPhoneAndHospital_Id(String phone, UUID hospitalId);

    boolean existsByPhoneAndHospital_IdAndIdNot(String phone, UUID hospitalId, UUID excludeId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID excludeId);
}
