package com.medcore.hms.doctor.repository;

import com.medcore.hms.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    Optional<Doctor> findByUser_Id(UUID userId);

    List<Doctor> findByHospital_Id(UUID hospitalId);

    List<Doctor> findByDepartment_Id(UUID departmentId);

    List<Doctor> findByHospital_IdAndIsAvailableTrue(UUID hospitalId);

    boolean existsByLicenseNumber(String licenseNumber);
}
