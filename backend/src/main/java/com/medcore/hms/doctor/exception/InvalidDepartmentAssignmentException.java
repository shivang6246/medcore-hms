package com.medcore.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidDepartmentAssignmentException extends RuntimeException {
    public InvalidDepartmentAssignmentException(UUID departmentId, UUID hospitalId) {
        super("Department " + departmentId + " does not belong to hospital " + hospitalId + ".");
    }
}
