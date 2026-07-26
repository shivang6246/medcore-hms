package com.medcore.hms.department.repository;

import com.medcore.hms.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByHospital_Id(UUID hospitalId);

    List<Department> findByHospital_IdAndIsActiveTrue(UUID hospitalId);

    boolean existsByHospital_IdAndName(UUID hospitalId, String name);
}
