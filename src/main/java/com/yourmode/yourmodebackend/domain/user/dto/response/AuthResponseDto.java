package com.yourmode.yourmodebackend.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "accessToken", "user" })
public class AuthResponseDto {
    private String accessToken;
    private UserInfoDto user;
}
