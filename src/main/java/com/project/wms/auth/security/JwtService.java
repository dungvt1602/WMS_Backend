package com.project.wms.auth.security;

import java.security.Key;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY = "caigicungduocnhungphaitren32kytu_chuoinaoconhieukytusaubanseduatoidaohon";

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername()) // để nhận biết người nào
                .issuedAt(new Date(System.currentTimeMillis())) // thời gian tạo token
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24)) // thời gian hết hạn token
                .signWith(getSignInKey())
                .compact();
    }

    // lay tên user từ token
    // private String extractUsername(String token) {
    // try {
    // return Jwts.parserBuilder()
    // .setSigningKey(getSignInKey())
    // .build()
    // .parseClaimsJws(token)
    // .getBody()
    // .getSubject();
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    // }

    private Key getSignInKey() {
        byte[] keyByte = Decoders.BASE64.decode(SECRET_KEY); // chuyển chuỗi thành dạng nhị phân
        return Keys.hmacShaKeyFor(keyByte); // tạo key từ mã nhị phân đó
    }
}
