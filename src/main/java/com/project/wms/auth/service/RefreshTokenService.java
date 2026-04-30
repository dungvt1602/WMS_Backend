package com.project.wms.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.project.wms.auth.config.JwtTokenProperties;
import com.project.wms.auth.dto.LogoutRequest;
import com.project.wms.auth.dto.TokenPairResponse;
import com.project.wms.auth.dto.TokenRefreshRequest;
import com.project.wms.auth.entity.RefreshToken;
import com.project.wms.auth.repository.RefreshTokenRepository;
import com.project.wms.auth.security.JwtService;
import com.project.wms.common.exception.BussinessException;
import com.project.wms.common.exception.RefreshTokenExpiredException;
import com.project.wms.common.exception.RefreshTokenReuseDetectedException;
import com.project.wms.common.exception.RefreshTokenRevokedException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtTokenProperties jwtTokenProperties;

    @Transactional
    public TokenPairResponse refresh(TokenRefreshRequest request, String clientIp, String userAgent) {
        String oldTokenHash = hashToken(request.refreshToken());

        RefreshToken current = refreshTokenRepository.findByTokenHash(oldTokenHash)
                .orElseThrow(() -> new BussinessException("Refresh token không hợp lệ"));

        if (current.isRevoked()) {
            if (current.getReplacedByTokenHash() != null && !current.getReplacedByTokenHash().isBlank()) {
                throw new RefreshTokenReuseDetectedException("REFRESH_TOKEN_REUSE_DETECTED");
            }
            throw new RefreshTokenRevokedException("REFRESH_TOKEN_REVOKED");
        }

        if (current.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("REFRESH_TOKEN_EXPIRED");
        }

        String newRawRefreshToken = generateRefreshToken();
        String newHashRefreshToken = hashToken(newRawRefreshToken);

        current.setRevoked(true);
        current.setReplacedByTokenHash(newHashRefreshToken);
        refreshTokenRepository.save(current);

        RefreshToken rotated = new RefreshToken();
        rotated.setUser(current.getUser());
        rotated.setTokenHash(newHashRefreshToken);
        rotated.setExpiresAt(LocalDateTime.now().plusSeconds(jwtTokenProperties.refreshTokenExpirationMs() / 1000));
        rotated.setRevoked(false);
        rotated.setCreatedByIp(clientIp);
        rotated.setUserAgent(userAgent);
        refreshTokenRepository.save(rotated);

        String newAccessToken = jwtService.generateToken(current.getUser());
        return new TokenPairResponse(
                newAccessToken,
                newRawRefreshToken,
                jwtTokenProperties.accessTokenExpirationMs(),
                jwtTokenProperties.refreshTokenExpirationMs(),
                current.getUser().getUsername());
    }

    @Transactional
    public void logout(LogoutRequest request, String clientIp, String userAgent) {
        String tokenHash = hashToken(request.refreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            }
        });
    }

    private String generateRefreshToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Không thể hash refresh token", e);
        }
    }
}
