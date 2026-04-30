package com.project.wms.common.exception;

public class RefreshTokenReuseDetectedException extends JwtAuthenticationException {
    public RefreshTokenReuseDetectedException(String message) {
        super(message);
    }
}
