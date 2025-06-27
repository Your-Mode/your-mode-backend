package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.LocalLoginRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.LocalSignupRequestDto;

public interface AuthService {
    AuthResponseDto signUp(LocalSignupRequestDto request);
    AuthResponseDto login(LocalLoginRequestDto request);
}
