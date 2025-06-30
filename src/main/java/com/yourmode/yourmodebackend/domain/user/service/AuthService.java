package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto signUp(LocalSignupRequestDto request);
    AuthResponseDto login(LocalLoginRequestDto request);
    AuthResponseDto processKakaoLogin(KakaoLoginRequestDto request);
    AuthResponseDto completeSignupWithKakao(KakaoSignupRequestDto request);
    AuthResponseDto refreshAccessToken(RefreshTokenRequestDto request);
}
