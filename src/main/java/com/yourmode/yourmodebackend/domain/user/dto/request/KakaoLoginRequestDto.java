package com.yourmode.yourmodebackend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 로그인 요청 DTO")
public class KakaoLoginRequestDto {

    @Schema(description = "카카오에서 받은 인증 코드", example = "AQABAAE...코드내용...")
    @NotBlank(message = "authorizationCode는 필수입니다.")
    private String authorizationCode;
}
