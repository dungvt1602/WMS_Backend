package com.project.wms.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        String secret,
        long accessTokenExpirationMs,
        long refreshTokenExpirationMs) {
}

