package com.medcore.hms.user.repository;

import com.medcore.hms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByHospital_Id(UUID hospitalId);

    /** Returns only non-deleted users for a given hospital. */
    @Query("SELECT u FROM User u WHERE u.hospital.id = :hospitalId AND u.deletedAt IS NULL")
    List<User> findActiveByHospitalId(@Param("hospitalId") UUID hospitalId);
}
