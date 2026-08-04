package com.medcore.hms.department.dto;

import java.util.UUID;

public record DepartmentSummaryDto(
        UUID id,
        String name,
        String description,
        Boolean isActive
) {}
