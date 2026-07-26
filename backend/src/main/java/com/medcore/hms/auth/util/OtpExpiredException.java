package com.medcore.hms.auth.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException() {
        super("OTP has expired. Please request a new one.");
    }
}
