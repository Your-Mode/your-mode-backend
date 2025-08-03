package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResultDto signUp(LocalSignupRequestDto request);
    AuthResultDto login(LocalLoginRequestDto request);
    AuthResultDto processKakaoLogin(KakaoLoginRequestDto request);
    AuthResultDto completeSignupWithKakao(KakaoSignupRequestDto request);
    AuthResultDto refreshAccessToken(HttpServletRequest request);
    UserIdResponseDto logout(PrincipalDetails principal);
}
