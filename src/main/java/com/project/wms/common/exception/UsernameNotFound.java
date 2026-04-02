package com.project.wms.common.exception;

public class UsernameNotFound extends RuntimeException {
    public UsernameNotFound(String message) {
        super(message);
    }

    public UsernameNotFound(String message, Throwable cause) {
        super(message, cause);
    }
}
