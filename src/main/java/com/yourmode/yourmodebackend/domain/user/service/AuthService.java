package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.KakaoSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalLoginRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalSignupRequestDto;

public interface AuthService {
    AuthResponseDto signUp(LocalSignupRequestDto request);
    AuthResponseDto login(LocalLoginRequestDto request);
    AuthResponseDto processKakaoLogin(String authorizationCode);
    AuthResponseDto completeSignupWithKakao(KakaoSignupRequestDto request);
}
