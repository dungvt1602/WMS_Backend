package com.project.wms.auth.security;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // để nhận biết người nào
                .issuedAt(new Date(System.currentTimeMillis())) // thời gian tạo token
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // thời gian hết hạn token
                .signWith(getSignInKey())
                .compact();
    }

    // lay tên user từ token
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
        // Tránh lỗi NullPointerException nếu token bị sai/hết hạn và username trả về
        // null
        // Đồng thời phải kiểm tra xem token còn hạn không
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
            // Nếu lỗi (hết hạn, sai key...) thì coi như token không còn hiệu lực
            return true;
        }

    }

    private SecretKey getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(secretKey); // chuyển chuỗi thành dạng nhị phân
        return Keys.hmacShaKeyFor(keyByte); // tạo key từ mã nhị phân đó
    }
}
