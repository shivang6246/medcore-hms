package com.medcore.hms.medicalrecord.repository;

import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    Page<MedicalRecord> findByPatient_Id(UUID patientId, Pageable pageable);

    Page<MedicalRecord> findByDoctor_Id(UUID doctorId, Pageable pageable);

    Optional<MedicalRecord> findByAppointment_Id(UUID appointmentId);

    boolean existsByAppointment_Id(UUID appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patient.id = :patientId AND m.isActive = true")
    Page<MedicalRecord> findActiveByPatientId(@Param("patientId") UUID patientId, Pageable pageable);

    @Query("SELECT m FROM MedicalRecord m WHERE m.doctor.id = :doctorId AND m.isActive = true")
    Page<MedicalRecord> findActiveByDoctorId(@Param("doctorId") UUID doctorId, Pageable pageable);
}
