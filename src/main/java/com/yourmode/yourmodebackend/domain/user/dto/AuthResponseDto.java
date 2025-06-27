package com.yourmode.yourmodebackend.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "accessToken", "refreshToken", "user", "additionalInfoNeeded" })
public class AuthResponseDto {
    private String accessToken;
    private String refreshToken;
    private UserInfoDto user;

    // 신규 회원 추가 정보 요청용 필드 추가
    private KakaoSignupRequestDto additionalInfoNeeded;

    // 카카오 로그인 신규 회원이라 추가 정보 입력이 필요할 때 반환하는 정적 팩토리 메서드
    public static AuthResponseDto ofNeedAdditionalInfo(KakaoSignupRequestDto kakaoSignupRequest) {
        return AuthResponseDto.builder()
                .additionalInfoNeeded(kakaoSignupRequest)
                .build();
    }
}
