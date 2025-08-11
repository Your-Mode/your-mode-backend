package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.service.AuthService;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "OAuth: 콜백 처리", description = "OAuth 인증 콜백 처리 API")
public class OAuthCallbackController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.same-site:lax}")
    private String cookieSameSite;

    @Operation(
            summary = "카카오 로그인 콜백 처리",
            description = "카카오 인증 후 리다이렉트되는 콜백 URL입니다. 인가 코드를 받아 로그인 처리를 수행합니다."
    )
    @GetMapping("/oauth2/kakao/callback")
    public ResponseEntity<BaseResponse<AuthResponseDto>> handleKakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response
    ) {
        // 에러 파라미터가 있으면 에러 처리
        if (error != null) {
            throw new RestApiException(UserErrorStatus.KAKAO_AUTH_DENIED);
        }
        
        // 인가 코드가 없으면 에러
        if (code == null || code.isEmpty()) {
            throw new RestApiException(UserErrorStatus.KAKAO_AUTH_CODE_MISSING);
        }
        
        // 카카오 로그인 처리
        AuthResultDto authResult = authService.handleKakaoCallback(code);

        // 기존 사용자인 경우 리프레시 토큰만 쿠키에 설정
        if (authResult.tokenPair().refreshToken() != null) {
            setRefreshTokenCookie(response, authResult.tokenPair().refreshToken());
        }

        // 액세스 토큰은 응답 바디로 반환 (신규 사용자는 null 가능)
        AuthResponseDto authResponseDto = AuthResponseDto.builder()
                .accessToken(authResult.tokenPair().accessToken())
                .user(authResult.userInfo())
                .build();

        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000));
        
        response.addCookie(refreshTokenCookie);
    }
}
