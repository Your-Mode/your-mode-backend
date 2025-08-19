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
@Tag(name = "OAuth: 카카오 인증", description = "카카오 OAuth 인증 관련 API")
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
    @Operation(summary = "카카오 회원가입 완료 처리", description = "추가 프로필 정보가 포함된 가입 요청을 받아 신규 회원으로 사용자 등록 및 인증 토큰을 쿠키로 설정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    summary = "회원가입 후 유저 정보 반환 (토큰은 쿠키로 설정됨)",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "user": {
                            "name": "홍길동",
                            "role": "USER"
                        },
                        "additionalInfoNeeded": null
                    }
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터 (바디타입 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "존재하지 않는 바디타입",
                                    summary = "존재하지 않는 바디타입 ID로 회원가입 시도",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:45:00.789",
                    "code": "AUTH-400-003",
                    "message": "존재하지 않는 체형입니다. 체형을 다시 선택해주세요."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "중복 정보(이메일 또는 전화번호)로 인한 회원가입 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "이메일 중복 오류",
                                            summary = "이미 가입된 이메일로 회원가입 시도",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:35:01.123",
                    "code": "AUTH-409-001",
                    "message": "이미 사용 중인 이메일입니다."
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "전화번호 중복 오류",
                                            summary = "이미 사용 중인 전화번호로 회원가입 시도",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:36:22.456",
                    "code": "AUTH-409-002",
                    "message": "이미 사용 중인 전화번호입니다."
                }
                """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "회원가입 도중 DB 저장 중 오류 발생",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "DB Insert 오류 - User",
                                            summary = "User 테이블 저장 실패 시",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:35:02.456",
                        "code": "AUTH-500-001",
                        "message": "사용자 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "DB Insert 오류 - Credential",
                                            summary = "UserCredential 저장 실패 시",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:35:03.789",
                        "code": "AUTH-500-002",
                        "message": "사용자 인증정보 저장 중 오류가 발생했습니다."
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "DB Insert 오류 - Profile",
                                            summary = "UserProfile 저장 실패 시",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:35:04.321",
                        "code": "AUTH-500-003",
                        "message": "사용자 프로필 저장 중 오류가 발생했습니다."
                    }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "DB Insert 오류 - Token",
                                            summary = "Refresh Token 저장 실패 시",
                                            value = """
                    {
                        "timestamp": "2025-06-29T12:35:05.654",
                        "code": "AUTH-500-004",
                        "message": "사용자 토큰 저장 중 오류가 발생했습니다."
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

