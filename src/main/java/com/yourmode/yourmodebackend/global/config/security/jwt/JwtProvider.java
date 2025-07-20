package com.yourmode.yourmodebackend.global.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    public record JwtWithExpiry(String token, LocalDateTime expiry) {}

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public JwtWithExpiry generateAccessToken(Integer userId, String email) {
        return generateTokenWithExpiry(userId, email, accessTokenExpiration);
    }

    public JwtWithExpiry generateRefreshToken(Integer userId, String email) {
        return generateTokenWithExpiry(userId, email, refreshTokenExpiration);
    }

    private JwtWithExpiry generateTokenWithExpiry(Integer userId,  String email, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId)) // userId는 subject로 저장
                .claim("email", email)              // email은 claim으로 저장
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        LocalDateTime expiryLocalDateTime = expiryDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return new JwtWithExpiry(token, expiryLocalDateTime);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Integer.parseInt(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }

    public Integer extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        if (!validateToken(token)) {
            throw new JwtException("Invalid or expired JWT token");
        }
        return getUserIdFromToken(token);
    }

    public String extractEmail(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }
        String token = authorizationHeader.substring(7);
        if (!validateToken(token)) {
            throw new JwtException("Invalid or expired JWT token");
        }
        return getEmailFromToken(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date()); // 현재 시간보다 이전이면 만료된 것
        } catch (ExpiredJwtException e) {
            return true; // 명백히 만료된 경우
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 만료 외 다른 문제 (ex. 변조, 서명 오류 등) → validateToken에서 처리
        }
    }


}
