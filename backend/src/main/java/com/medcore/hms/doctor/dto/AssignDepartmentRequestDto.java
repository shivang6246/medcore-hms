package com.medcore.hms.doctor.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignDepartmentRequestDto(
        @NotNull(message = "Department ID is required")
        UUID departmentId
) {}
