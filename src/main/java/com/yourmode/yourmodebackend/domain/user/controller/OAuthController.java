package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.request.KakaoSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.service.OAuthService;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth(OAuth): 카카오 인증", description = "카카오 OAuth 인증 관련 API")
public class OAuthController {

    private final OAuthService oauthService;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.same-site:lax}")
    private String cookieSameSite;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${redirect.new-user}")
    private String newUserRedirectUrl;

    @Value("${redirect.existing-user}")
    private String existingUserRedirectUrl;

    /**
     * 카카오 로그인 인증 요청 엔드포인트
     */
    @Operation(
            summary = "카카오 로그인 인증 요청",
            description = "카카오 인증 URL을 반환합니다."
    )
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/oauth2/kakao/authorize")
    public ResponseEntity<BaseResponse<String>> getKakaoAuthUrl() {
        String authUrl = "https://kauth.kakao.com/oauth/authorize?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code";
        return ResponseEntity.ok(BaseResponse.onSuccess(authUrl));
    }

    @Operation(
            summary = "카카오 로그인 콜백 처리",
            description = "카카오 인증 후 리다이렉트되는 콜백 URL입니다. 신규회원은 회원가입 페이지로, 기존회원은 메인 페이지로 리다이렉트됩니다."
    )
    @GetMapping("/oauth2/kakao/callback") 
    public void handleKakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response
    ) throws Exception {
        // 에러 파라미터가 있으면 에러 처리
        if (error != null) {
            throw new RestApiException(UserErrorStatus.KAKAO_AUTH_DENIED);
        }
        
        // 인가 코드가 없으면 에러
        if (code == null || code.isEmpty()) {
            throw new RestApiException(UserErrorStatus.KAKAO_AUTH_CODE_MISSING);
        }
        
        // 카카오 로그인 처리
        AuthResultDto authResult = oauthService.handleKakaoCallback(code);

        // 신규회원과 기존회원에 따라 다른 URL로 리다이렉트
        if (Boolean.TRUE.equals(authResult.userInfo().getIsNewUser())) {
            // 신규회원: 회원가입 완료 페이지로 리다이렉트 (이메일과 닉네임 정보 포함)
            String redirectUrl = newUserRedirectUrl + 
                "?email=" + URLEncoder.encode(authResult.userInfo().getEmail(), "UTF-8") +
                "&nickname=" + URLEncoder.encode(authResult.userInfo().getName(), "UTF-8");
            response.sendRedirect(redirectUrl);
        } else {
            // 기존회원: 메인 페이지로 리다이렉트 (토큰을 쿠키에 설정)
            if (authResult.tokenPair().refreshToken() != null) {
                setRefreshTokenCookie(response, authResult.tokenPair().refreshToken());
            }
            // 쿠키 설정 후 리다이렉트
            response.sendRedirect(existingUserRedirectUrl);
        }
    }

    /**
     * 카카오 회원가입 완료 처리
     */
    @Operation(
            summary = "카카오 회원가입 완료",
            description = "카카오 OAuth 인증 후 추가 정보 입력을 완료하여 회원가입을 완료합니다. 리프레시 토큰은 쿠키로, 액세스 토큰은 응답 바디로 반환됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "회원가입 성공",
                                            summary = "카카오 회원가입 완료 후 토큰 발급",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:30:00.000",
                        "code": "200",
                        "message": "요청이 성공적으로 처리되었습니다.",
                        "data": {
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "user": {
                                "id": 1,
                                "email": "user@example.com",
                                "nickname": "사용자",
                                "isNewUser": false
                            }
                        }
                    }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "유효성 검증 실패",
                                            summary = "요청 데이터 유효성 검증 실패",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:30:00.000",
                        "code": "AUTH-400-001",
                        "message": "요청 데이터가 유효하지 않습니다."
                    }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 데이터 충돌",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이메일 중복",
                                            summary = "이미 사용 중인 이메일로 회원가입 시도",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:30:00.000",
                        "code": "AUTH-409-001",
                        "message": "이미 사용 중인 이메일입니다."
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "닉네임 중복",
                                            summary = "이미 사용 중인 닉네임으로 회원가입 시도",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:30:00.000",
                        "code": "AUTH-409-002",
                        "message": "이미 사용 중인 닉네임입니다."
                    }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "서버 오류",
                                            summary = "회원가입 처리 중 서버 오류 발생",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:30:00.000",
                        "code": "AUTH-500-001",
                        "message": "회원가입 처리 중 오류가 발생했습니다."
                    }
                    """
                                    )
                            }
                    )
            )
    })
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/oauth2/kakao/signup/complete")
    public ResponseEntity<BaseResponse<AuthResponseDto>> completeSignupWithKakao(
            @Valid @RequestBody KakaoSignupRequestDto request,
            HttpServletResponse response
    ) {
        AuthResultDto authResult = oauthService.completeSignupWithKakao(request);

        // 리프레시 토큰을 쿠키로 설정
        setRefreshTokenCookie(response, authResult.tokenPair().refreshToken());

        // 액세스 토큰은 응답 바디로 반환
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

