package com.medcore.hms.hospital.repository;

import com.medcore.hms.hospital.entity.Hospital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, UUID>, JpaSpecificationExecutor<Hospital> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"address"})
    Optional<Hospital> findById(@NonNull UUID id);

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"address"})
    Page<Hospital> findAll(@Nullable Specification<Hospital> spec, @NonNull Pageable pageable);

    Optional<Hospital> findByRegistrationNumber(String registrationNumber);

    Optional<Hospital> findByLicenseNumber(String licenseNumber);

    Optional<Hospital> findByEmail(String email);

    List<Hospital> findByIsActiveTrue();

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);
}
