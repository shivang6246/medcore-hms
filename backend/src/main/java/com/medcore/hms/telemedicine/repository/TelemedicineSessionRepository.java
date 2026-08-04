package com.medcore.hms.telemedicine.repository;

import com.medcore.hms.telemedicine.entity.ConsultationSessionStatus;
import com.medcore.hms.telemedicine.entity.TelemedicineSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelemedicineSessionRepository extends JpaRepository<TelemedicineSession, UUID> {

    Optional<TelemedicineSession> findByAppointment_Id(UUID appointmentId);

    Optional<TelemedicineSession> findByRoomCode(String roomCode);

    List<TelemedicineSession> findByDoctor_IdAndStatus(UUID doctorId, ConsultationSessionStatus status);

    Page<TelemedicineSession> findByDoctor_Id(UUID doctorId, Pageable pageable);

    Page<TelemedicineSession> findByPatient_Id(UUID patientId, Pageable pageable);

    boolean existsByRoomCode(String roomCode);
}
