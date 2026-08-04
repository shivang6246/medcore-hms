package com.medcore.hms.ipd.repository;

import com.medcore.hms.ipd.entity.Bed;
import com.medcore.hms.ipd.entity.BedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BedRepository extends JpaRepository<Bed, UUID> {

    List<Bed> findByStatusAndIsActiveTrue(BedStatus status);

    Page<Bed> findByStatusAndIsActiveTrue(BedStatus status, Pageable pageable);

    @Query("SELECT b FROM Bed b WHERE b.room.ward.id = :wardId AND b.status = 'AVAILABLE' AND b.isActive = true")
    List<Bed> findAvailableBedsByWard(UUID wardId);
}
