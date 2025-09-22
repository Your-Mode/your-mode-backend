package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.KakaoSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;

import java.util.Map;

public interface OAuthService {
    Map<String, Object> requestTokenWithKakao(String authorizationCode);
    Map<String, Object> requestUserInfoWithKakao(String accessToken);
    AuthResultDto handleKakaoCallback(String code);
    AuthResultDto completeSignupWithKakao(KakaoSignupRequestDto request);
}
