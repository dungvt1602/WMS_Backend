package com.project.wms.common.exception;

public class LoginRateLimitExceededException extends RuntimeException {
    public LoginRateLimitExceededException(String message) {
        super(message);
    }
}
