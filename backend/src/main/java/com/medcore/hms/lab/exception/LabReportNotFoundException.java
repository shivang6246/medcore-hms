package com.medcore.hms.lab.exception;

import java.util.UUID;

public class LabReportNotFoundException extends RuntimeException {
    public LabReportNotFoundException(String message) {
        super(message);
    }

    public LabReportNotFoundException(UUID labTestId) {
        super("Lab report not found for lab test ID: " + labTestId);
    }
}
