package com.yourmode.yourmodebackend.domain.user.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserInfoDto user;
}
