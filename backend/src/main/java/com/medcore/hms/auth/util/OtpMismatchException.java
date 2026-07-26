package com.medcore.hms.auth.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OtpMismatchException extends RuntimeException {
    public OtpMismatchException() {
        super("Invalid OTP. Please check and try again.");
    }
}
