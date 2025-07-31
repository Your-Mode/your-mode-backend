package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResult signUp(LocalSignupRequestDto request);
    AuthResult login(LocalLoginRequestDto request);
    AuthResult processKakaoLogin(KakaoLoginRequestDto request);
    AuthResult completeSignupWithKakao(KakaoSignupRequestDto request);
    AuthResult refreshAccessToken(HttpServletRequest request);
    UserIdResponseDto logout(PrincipalDetails principal);

    // 토큰 쌍을 위한 간단한 레코드
    record TokenPair(String accessToken, String refreshToken) {}
    
    // 인증 결과를 위한 레코드 (토큰과 사용자 정보 포함)
    record AuthResult(TokenPair tokenPair, UserInfoDto userInfo) {}
}
