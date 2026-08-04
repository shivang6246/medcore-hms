package com.medcore.hms.doctor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmployeeIdException extends DoctorAlreadyExistsException {
    public DuplicateEmployeeIdException(String employeeId, UUID hospitalId) {
        super("Employee ID '" + employeeId + "' already exists in hospital " + hospitalId + ".");
    }
}
