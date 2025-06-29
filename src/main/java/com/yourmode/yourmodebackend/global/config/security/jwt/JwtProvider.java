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

    public JwtWithExpiry generateAccessToken(Long userId, String email) {
        return generateTokenWithExpiry(userId, email, accessTokenExpiration);
    }

    public JwtWithExpiry generateRefreshToken(Long userId, String email) {
        return generateTokenWithExpiry(userId, email, refreshTokenExpiration);
    }

    private JwtWithExpiry generateTokenWithExpiry(Long userId,  String email, long expirationMillis) {
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

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("email", String.class);
    }

    public Long extractUserId(String authorizationHeader) {
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

}
