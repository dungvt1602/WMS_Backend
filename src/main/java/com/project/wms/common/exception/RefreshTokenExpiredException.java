package com.project.wms.common.exception;

public class RefreshTokenExpiredException extends JwtAuthenticationException {
    public RefreshTokenExpiredException(String message) {
        super(message);
    }
}
