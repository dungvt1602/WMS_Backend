package com.project.wms.common.exception;

public class RefreshTokenRevokedException extends JwtAuthenticationException {
    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
