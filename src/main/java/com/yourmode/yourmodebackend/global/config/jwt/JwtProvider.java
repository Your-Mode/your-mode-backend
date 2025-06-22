package com.yourmode.yourmodebackend.global.common.auth;

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

    public JwtWithExpiry generateAccessToken(Long userId) {
        return generateTokenWithExpiry(userId, accessTokenExpiration);
    }

    public JwtWithExpiry generateRefreshToken(Long userId) {
        return generateTokenWithExpiry(userId, refreshTokenExpiration);
    }

    private JwtWithExpiry generateTokenWithExpiry(Long userId, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        String token = Jwts.builder()
                .setSubject(String.valueOf(userId))
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
}
