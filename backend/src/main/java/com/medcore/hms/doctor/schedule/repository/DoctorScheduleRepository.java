package com.medcore.hms.doctor.schedule.repository;

import com.medcore.hms.doctor.schedule.entity.DayOfWeek;
import com.medcore.hms.doctor.schedule.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"doctor"})
    Optional<DoctorSchedule> findById(@NonNull UUID id);

    List<DoctorSchedule> findByDoctor_Id(UUID doctorId);

    List<DoctorSchedule> findByDoctor_IdAndIsActiveTrue(UUID doctorId);

    Optional<DoctorSchedule> findByDoctor_IdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);

    Optional<DoctorSchedule> findByDoctor_IdAndDayOfWeekAndIsActiveTrue(UUID doctorId, DayOfWeek dayOfWeek);

    boolean existsByDoctor_IdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);
}
