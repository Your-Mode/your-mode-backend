package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.service.AuthService;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth: 회원가입, 로그인", description = "인증 관련 API (회원가입, 로그인 등)")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.same-site:lax}")
    private String cookieSameSite;

    /**
     * 토큰을 쿠키로 설정하는 헬퍼 메서드
     */
    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // 액세스 토큰 쿠키 설정
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure); // 환경에 따라 설정
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) (accessTokenExpiration / 1000)); // 초 단위로 변환
        
        // SameSite 속성 설정 (Servlet 4.0+)
        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            accessTokenCookie.setAttribute("SameSite", cookieSameSite);
        }
        
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰 쿠키 설정
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure); // 환경에 따라 설정
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (refreshTokenExpiration / 1000)); // 초 단위로 변환
        
        // SameSite 속성 설정 (Servlet 4.0+)
        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            refreshTokenCookie.setAttribute("SameSite", cookieSameSite);
        }
        
        response.addCookie(refreshTokenCookie);
    }

    /**
     * 로컬 회원가입
     *
     * @param request LocalSignupRequestDto - 회원가입 요청 데이터 (이메일, 비밀번호 등)
     * @param response 쿠키 설정을 위한 HttpServletResponse
     * @return 유저 정보를 포함한 응답 DTO
     */
    @Operation(summary = "로컬 회원가입", description = "이메일과 비밀번호를 통해 회원가입을 수행하고, JWT 토큰을 쿠키로 설정합니다.")
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
                             "name": "string",
                             "email": "test1234@example.com",
                             "role": "USER",
                             "isNewUser": false
                        }
                    }
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
                        "message": "프로필 정보를 저장하는 도중 오류가 발생했습니다."
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
                    responseCode = "401",
                    description = "회원가입 후 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 후 인증 실패",
                                    summary = "회원가입은 성공했으나, 인증(로그인) 과정에서 실패",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:45:00.789",
                    "code": "AUTH-401-001",
                    "message": "인증에 실패했습니다."
                }
                """
                            )
                    )
            )
    })
        @PostMapping("/signup")
    public ResponseEntity<BaseResponse<AuthResponseDto>> signUp(
            @Valid @RequestBody LocalSignupRequestDto request,
            HttpServletResponse response
    ) {
        AuthResultDto authResult = authService.signUp(request);

        // 토큰을 쿠키로 설정
        setTokenCookies(response, authResult.tokenPair().accessToken(), authResult.tokenPair().refreshToken());

        // 사용자 정보를 AuthResult에서 가져와서 응답 구성
        AuthResponseDto authResponseDto = new AuthResponseDto(authResult.userInfo());

        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 로컬 로그인
     *
     * @param request LocalLoginRequestDto - 로그인 요청 데이터 (이메일, 비밀번호)
     * @param response 쿠키 설정을 위한 HttpServletResponse
     * @return 유저 정보를 포함한 응답 DTO
     */
    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호를 통해 로그인하고 JWT 토큰을 쿠키로 설정합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "로그인 성공 예시",
                                    summary = "로그인 후 유저 정보 반환 (토큰은 쿠키로 설정됨)",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "user": {
                            "name": "string",
                            "email": "test1234@example.com",
                            "role": "USER",
                            "isNewUser": false
                        }
                    }
                }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - 로그인 과정에서 인증 실패 또는 이메일/비밀번호 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "로그인 인증 실패",
                                            summary = "인증(로그인) 과정에서 실패",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:45:00.789",
                    "code": "AUTH-401-001",
                    "message": "인증에 실패했습니다."
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "이메일 또는 비밀번호 오류",
                                            summary = "이메일 또는 비밀번호가 올바르지 않음",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:45:01.789",
                    "code": "AUTH-401-002",
                    "message": "이메일 또는 비밀번호가 올바르지 않습니다."
                }
                """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<AuthResponseDto>> login(
            @Valid @RequestBody LocalLoginRequestDto request,
            HttpServletResponse response
    ) {
        AuthResultDto authResult = authService.login(request);

        // 토큰을 쿠키로 설정
        setTokenCookies(response, authResult.tokenPair().accessToken(), authResult.tokenPair().refreshToken());

        // 사용자 정보를 AuthResult에서 가져와서 응답 구성
        AuthResponseDto authResponseDto = new AuthResponseDto(authResult.userInfo());

        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    @Value("${kakao.client-id}")
    private String clientId;
    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 카카오 로그인 인증 요청 엔드포인트
     * <p>
     * 1. 클라이언트가 GET /authorize 요청을 보냅니다.
     * 2. 이 메서드는 카카오 인증 URL을 반환합니다.
     * 3. 클라이언트는 이 URL로 리다이렉트하여 카카오 인증을 진행합니다.
     * 4. 사용자가 로그인 및 동의를 완료하면, Kakao가 Authorization Code를 포함해 리다이렉션합니다.
     * 5. 서버는 code를 받아 access token을 요청하고 로그인 처리합니다.
     *
     * @return 카카오 인증 URL
     */
    @Operation(
            summary = "카카오 로그인 인증 요청",
            description = "카카오 인증 URL을 반환합니다. 프론트엔드에서 이 URL로 리다이렉트하여 카카오 로그인을 진행합니다."
    )
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/oauth2/kakao/authorize")
    public ResponseEntity<BaseResponse<String>> getKakaoAuthUrl() {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        return ResponseEntity.ok(BaseResponse.onSuccess(kakaoAuthUrl));
    }

    @Operation(
            summary = "카카오 로그인 콜백 처리",
            description = "카카오 인증 후 리다이렉트되는 콜백 URL입니다. 인가 코드를 받아 로그인 처리를 수행합니다. 신규 사용자인 경우 추가 정보 입력이 필요하며, 기존 사용자인 경우 바로 로그인됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 또는 추가 정보 필요",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "기존 사용자 로그인 성공",
                                            summary = "기존 사용자 카카오 로그인 성공 (토큰 설정됨)",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "user": {
                            "name": "홍길동",
                            "email": "user@example.com",
                            "role": "USER",
                            "isNewUser": false
                        }
                    }
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "신규 사용자 추가 정보 필요",
                                            summary = "신규 사용자 카카오 로그인 (추가 정보 입력 필요)",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:35:00.123",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "user": {
                            "name": "신규 사용자",
                            "email": "newuser@example.com",
                            "role": "USER",
                            "isNewUser": true
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
                    description = "인가 코드 누락 또는 잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "인가 코드 누락",
                                    summary = "인가 코드가 없는 경우",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:45:00.000",
                    "code": "AUTH-400-001",
                    "message": "인가 코드가 필요합니다."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "카카오 API 호출 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "카카오 API 실패",
                                    summary = "카카오 토큰 또는 사용자 정보 요청 실패",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:45:00.000",
                    "code": "KAKAO-502-001",
                    "message": "카카오 토큰 요청에 실패했습니다."
                }
                """
                            )
                    )
            )
    })
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
        
        // 기존 사용자인 경우 토큰을 쿠키에 설정
        if (authResult.tokenPair().accessToken() != null && authResult.tokenPair().refreshToken() != null) {
            setTokenCookies(response, authResult.tokenPair().accessToken(), authResult.tokenPair().refreshToken());
        }
        
        // AuthResponseDto로 변환하여 응답
        AuthResponseDto authResponseDto = new AuthResponseDto(authResult.userInfo());
        
        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 카카오 회원가입 완료 처리.
     * <p>
     * 클라이언트로부터 추가 프로필 정보가 포함된 가입 요청을 받아
     * 신규 회원으로 사용자 등록 및 인증 토큰을 발급.
     *
     * @param request KakaoSignupRequestDto - 카카오 회원가입 요청 DTO (추가 프로필 정보 포함)
     * @param response 쿠키 설정을 위한 HttpServletResponse
     * @return 회원가입 완료 후 유저 정보가 포함된 응답 반환
     *
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
                        "message": "프로필 정보를 저장하는 도중 오류가 발생했습니다."
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
                        AuthResultDto authResult = authService.completeSignupWithKakao(request);

        // 토큰을 쿠키로 설정
        setTokenCookies(response, authResult.tokenPair().accessToken(), authResult.tokenPair().refreshToken());

        // 사용자 정보를 AuthResult에서 가져와서 응답 구성
        AuthResponseDto authResponseDto = new AuthResponseDto(authResult.userInfo());

        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급 처리
     *
     * @param request HttpServletRequest - 쿠키에서 리프레시 토큰을 추출
     * @param response 쿠키 설정을 위한 HttpServletResponse
     * @return 새로 발급된 유저 정보가 포함된 응답
     *
     */
    @Operation(summary = "액세스 토큰 재발급", description = "쿠키의 리프레시 토큰으로 액세스 토큰 재발급 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "토큰 재발급 성공 예시",
                                    summary = "새로운 토큰이 쿠키로 설정되고 유저 정보 반환",
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
                    responseCode = "404",
                    description = "사용자 정보가 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "사용자 미존재",
                                    summary = "이메일은 존재하지만 사용자 정보가 없을 때 발생",
                                    value = """
            {
                "timestamp": "2025-06-29T12:45:00.000",
                "code": "USER-404-001",
                "message": "사용자를 찾을 수 없습니다."
            }
            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "토큰 정보가 유효하지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "유효하지 않은 토큰",
                                    summary = "토큰 검증 후 유효하지 않을 때 발생",
                                    value = """
            {
                "timestamp": "2025-06-29T12:45:00.000",
                "code": "USER-404-001",
                "message": "유효하지 않은 토큰입니다."
            }
            """
                            )
                    )
            ),
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<BaseResponse<AuthResponseDto>> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthResultDto authResult = authService.refreshAccessToken(request);

        // 토큰을 쿠키로 설정
        setTokenCookies(response, authResult.tokenPair().accessToken(), authResult.tokenPair().refreshToken());
        
        // 사용자 정보를 AuthResult에서 가져와서 응답 구성
        AuthResponseDto authResponseDto = new AuthResponseDto(authResult.userInfo());
        
        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 로그아웃 API
     * <p>
     * - 현재 로그인된 사용자의 리프레시 토큰을 삭제하여 로그아웃을 수행합니다.
     * - 쿠키에서 토큰을 삭제합니다.
     **/
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 리프레시 토큰을 삭제하여 로그아웃 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "로그아웃 성공 예시",
                                    summary = "성공적으로 로그아웃되어 사용자 ID 반환",
                                    value = """
                {
                  "timestamp": "2025-06-30T10:20:30.123",
                  "code": "COMMON200",
                  "message": "요청에 성공하였습니다.",
                  "result": {
                    "userId": 12345
                  }
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "활성 세션이 없는 상태에서 로그아웃 시도",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "활성 세션 없음",
                                    summary = "삭제할 리프레시 토큰이 없어 활성 세션이 없다고 판단할 때 발생",
                                    value = """
                {
                  "timestamp": "2025-06-30T10:25:00.000",
                  "code": "AUTH-400-002",
                  "message": "활성화된 세션이 존재하지 않습니다."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    summary = "로그아웃 처리 중 서버 오류 발생",
                                    value = """
                {
                  "timestamp": "2025-06-30T10:30:00.000",
                  "code": "AUTH-500-005",
                  "message": "로그아웃 처리 중 오류가 발생했습니다."
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<UserIdResponseDto>> logout(
            @CurrentUser PrincipalDetails principal,
            HttpServletResponse response
    ) {
        UserIdResponseDto userIdResponseDto = authService.logout(principal);
        
        // 쿠키에서 토큰 삭제
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        
        // SameSite 속성 설정
        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            accessTokenCookie.setAttribute("SameSite", cookieSameSite);
        }
        
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(cookieSecure);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        
        // SameSite 속성 설정
        if (cookieSameSite != null && !cookieSameSite.isEmpty()) {
            refreshTokenCookie.setAttribute("SameSite", cookieSameSite);
        }
        
        response.addCookie(refreshTokenCookie);
        
        return ResponseEntity.ok(BaseResponse.onSuccess(userIdResponseDto));
    }

}