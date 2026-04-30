package com.project.wms.auth.security;

import java.time.Instant;
import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.project.wms.auth.config.JwtTokenProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final JwtTokenProperties jwtTokenProperties;

    public JwtService(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtTokenProperties.accessTokenExpirationMs()))
                .signWith(getSignInKey())
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isTokenExpired(String token) {

        try {
            Date expiration = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expiration.toInstant().isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }

    }

    private SecretKey getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(jwtTokenProperties.secret());
        return Keys.hmacShaKeyFor(keyByte);
    }
}
