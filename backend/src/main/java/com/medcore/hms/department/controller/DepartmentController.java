package com.medcore.hms.department.controller;

import com.medcore.hms.common.dto.ApiResponse;
import com.medcore.hms.department.dto.DepartmentSummaryDto;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Departments", description = "Read-only department listing for hospital-scoped dropdowns.")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @Operation(summary = "List departments for a hospital")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DepartmentSummaryDto>>> listByHospital(
            @RequestParam UUID hospitalId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<Department> departments = activeOnly
                ? departmentRepository.findByHospital_IdAndIsActiveTrue(hospitalId)
                : departmentRepository.findByHospital_Id(hospitalId);
        List<DepartmentSummaryDto> result = departments.stream()
                .map(d -> new DepartmentSummaryDto(d.getId(), d.getName(), d.getDescription(), d.getIsActive()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(result, "Departments fetched successfully"));
    }
}
