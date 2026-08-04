package com.medcore.hms.doctor.slot.repository;

import com.medcore.hms.doctor.slot.entity.DoctorSlot;
import com.medcore.hms.doctor.slot.entity.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, UUID> {

    List<DoctorSlot> findByDoctor_IdAndSlotDateOrderByStartTime(UUID doctorId, LocalDate slotDate);

    List<DoctorSlot> findByDoctor_IdAndSlotDateBetweenOrderBySlotDateAscStartTimeAsc(
            UUID doctorId, LocalDate from, LocalDate to);

    List<DoctorSlot> findByDoctor_IdAndStatus(UUID doctorId, SlotStatus status);

    boolean existsByDoctor_IdAndSlotDateAndStartTime(UUID doctorId, LocalDate slotDate, LocalTime startTime);

    @Modifying
    @Query("DELETE FROM DoctorSlot s WHERE s.doctor.id = :doctorId AND s.slotDate = :slotDate AND s.status = 'AVAILABLE'")
    int deleteAvailableSlotsByDoctorAndDate(@Param("doctorId") UUID doctorId, @Param("slotDate") LocalDate slotDate);

    @Query("SELECT COUNT(s) FROM DoctorSlot s WHERE s.doctor.id = :doctorId AND s.slotDate BETWEEN :from AND :to")
    long countByDoctorAndDateRange(@Param("doctorId") UUID doctorId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
