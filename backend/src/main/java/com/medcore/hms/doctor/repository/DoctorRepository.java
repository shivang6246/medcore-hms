package com.medcore.hms.doctor.repository;

import com.medcore.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"user", "hospital", "department"})
    Optional<Doctor> findById(@NonNull UUID id);

    Optional<Doctor> findByEmail(String email);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    Optional<Doctor> findByUser_Id(UUID userId);

    List<Doctor> findByHospital_Id(UUID hospitalId);

    List<Doctor> findByDepartment_Id(UUID departmentId);

    List<Doctor> findByHospital_IdAndDepartment_Id(UUID hospitalId, UUID departmentId);

    List<Doctor> findByHospital_IdAndIsActiveTrue(UUID hospitalId);

    List<Doctor> findByIsActiveTrue();

    List<Doctor> findBySpecializationContainingIgnoreCase(String specialization);

    Optional<Doctor> findByEmployeeIdAndHospital_Id(String employeeId, UUID hospitalId);

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmployeeIdAndHospital_Id(String employeeId, UUID hospitalId);
}
