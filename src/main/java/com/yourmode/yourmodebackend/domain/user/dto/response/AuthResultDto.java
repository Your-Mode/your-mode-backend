package com.yourmode.yourmodebackend.domain.user.dto.response;

/**
 * 인증 결과를 담는 DTO
 * @param tokenPair 토큰 쌍 (access token, refresh token)
 * @param userInfo 사용자 정보
 */
public record AuthResultDto(TokenPairDto tokenPair, UserInfoDto userInfo) {} 