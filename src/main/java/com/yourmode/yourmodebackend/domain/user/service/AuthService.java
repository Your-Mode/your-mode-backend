package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;

public interface AuthService {
    AuthResponseDto signUp(LocalSignupRequestDto request);
    AuthResponseDto login(LocalLoginRequestDto request);
    AuthResponseDto processKakaoLogin(KakaoLoginRequestDto request);
    AuthResponseDto completeSignupWithKakao(KakaoSignupRequestDto request);
    AuthResponseDto refreshAccessToken(RefreshTokenRequestDto request);

    UserIdResponseDto logout(PrincipalDetails principal);
}
