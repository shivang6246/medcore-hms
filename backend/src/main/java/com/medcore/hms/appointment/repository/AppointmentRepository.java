package com.medcore.hms.appointment.repository;

import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"patient", "doctor", "hospital", "slot"})
    Optional<Appointment> findById(@NonNull UUID id);

    Page<Appointment> findByHospital_Id(UUID hospitalId, Pageable pageable);

    Page<Appointment> findByHospital_IdAndStatus(UUID hospitalId, AppointmentStatus status, Pageable pageable);

    Page<Appointment> findByDoctor_Id(UUID doctorId, Pageable pageable);

    Page<Appointment> findByPatient_Id(UUID patientId, Pageable pageable);

    List<Appointment> findByDoctor_IdAndAppointmentDateOrderByStartTime(UUID doctorId, LocalDate date);

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentDateDescStartTimeDesc(UUID patientId, AppointmentStatus status);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
              AND a.appointmentDate BETWEEN :from AND :to
            ORDER BY a.appointmentDate ASC, a.startTime ASC
            """)
    List<Appointment> findByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
            SELECT a FROM Appointment a
            WHERE (:hospitalId IS NULL OR a.hospital.id = :hospitalId)
              AND (:patientId  IS NULL OR a.patient.id  = :patientId)
              AND (:doctorId   IS NULL OR a.doctor.id   = :doctorId)
              AND (:status     IS NULL OR a.status      = :status)
              AND (:fromDate   IS NULL OR a.appointmentDate >= :fromDate)
              AND (:toDate     IS NULL OR a.appointmentDate <= :toDate)
            """)
    Page<Appointment> search(
            @Param("hospitalId") UUID hospitalId,
            @Param("patientId")  UUID patientId,
            @Param("doctorId")   UUID doctorId,
            @Param("status")     AppointmentStatus status,
            @Param("fromDate")   LocalDate fromDate,
            @Param("toDate")     LocalDate toDate,
            Pageable pageable);

    boolean existsBySlot_Id(UUID slotId);

    long countByDoctor_IdAndAppointmentDateAndStatusNot(UUID doctorId, LocalDate date, AppointmentStatus status);

    Optional<Appointment> findBySlot_Id(UUID slotId);

    boolean existsByAppointmentNumber(String appointmentNumber);
}
