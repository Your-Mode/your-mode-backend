package com.yourmode.yourmodebackend.domain.user.dto.response;

/**
 * 토큰 쌍을 담는 DTO
 * @param accessToken 액세스 토큰
 * @param refreshToken 리프레시 토큰
 */
public record TokenPairDto(String accessToken, String refreshToken) {} 