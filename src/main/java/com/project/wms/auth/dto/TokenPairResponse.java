package com.project.wms.auth.dto;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInMs,
        long refreshTokenExpiresInMs,
        String username) {
}
