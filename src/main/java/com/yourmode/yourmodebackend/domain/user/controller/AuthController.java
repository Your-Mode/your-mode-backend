package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.service.AuthService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth: 회원가입, 로그인", description = "인증 관련 API (회원가입, 로그인 등)")
public class AuthController {

    private final AuthService authService;

    /**
     * 로컬 회원가입
     *
     * @param request LocalSignupRequestDto - 회원가입 요청 데이터 (이메일, 비밀번호 등)
     * @return 액세스 토큰과 유저 정보를 포함한 응답 DTO
     */
    @Operation(summary = "로컬 회원가입", description = "이메일과 비밀번호를 통해 회원가입을 수행하고, JWT 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    summary = "회원가입 후 발급된 토큰과 유저 정보 반환",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                        "user": {
                            "name": "홍길동",
                            "role": "USER",
                            "bodyTypeId": 2
                        },
                        "additionalInfoNeeded": null
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
    public ResponseEntity<BaseResponse<AuthResponseDto>> signUp(@Valid @RequestBody LocalSignupRequestDto request) {
        AuthResponseDto authResponseDto = authService.signUp(request);
        return ResponseEntity.ok(
                BaseResponse.onSuccess(authResponseDto)
        );
    }

    /**
     * 로컬 로그인
     *
     * @param request LocalLoginRequestDto - 로그인 요청 데이터 (이메일, 비밀번호)
     * @return 액세스 토큰과 유저 정보를 포함한 응답 DTO
     */
    @Operation(summary = "로컬 로그인", description = "이메일과 비밀번호를 통해 로그인하고 JWT 토큰과 사용자 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(name = "로그인 성공 예시",
                                    summary = "로그인 후 발급된 토큰과 유저 정보 반환",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                        "user": {
                            "name": "홍길동",
                            "role": "USER",
                            "bodyTypeId": 2
                        },
                        "additionalInfoNeeded": null
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
    public ResponseEntity<BaseResponse<AuthResponseDto>> login(@Valid @RequestBody LocalLoginRequestDto request) {
        AuthResponseDto authResponseDto = authService.login(request);
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
     * 2. 이 메서드는 카카오 인증 URL을 구성한 뒤, 클라이언트를 해당 URL로 리다이렉트합니다.
     * 3. 사용자가 로그인 및 동의를 완료하면, Kakao가 Authorization Code를 포함해 리다이렉션합니다.
     * 4. 서버는 code를 받아 access token을 요청하고 로그인 처리합니다.
     *
     * @param response HttpServletResponse 객체로 리다이렉션 수행
     * @throws IOException 리다이렉션 중 예외 발생 시
     */
    @Operation(
            summary = "카카오 로그인 인증 요청",
            description = "클라이언트를 카카오 인증 페이지로 리다이렉트합니다."
    )
    @GetMapping("/oauth2/kakao/authorize")
    public void redirectToKakaoAuth(HttpServletResponse response) throws IOException {

        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        response.sendRedirect(kakaoAuthUrl);
    }

    /**
     * 카카오 로그인 요청 처리.
     *
     * @param request KakaoLoginRequestDto - 카카오 인가 코드를 담은 데이터
     * @param servletResponse 리프레시 토큰을 쿠키 등에 설정하기 위한 HttpServletResponse
     * @return 로그인 성공 시 액세스 토큰과 사용자 정보가 포함된 응답을 반환
     */
    @Operation(
            summary = "카카오 로그인 요청 처리",
            description = "인가 코드(code)를 받아 카카오 로그인 처리를 수행합니다. 신규 회원일 시 \"additionalInfoNeeded\"에 사용자 정보를 담아 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 (추가 정보 필요 여부에 따라 결과가 다름)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "추가 정보가 필요 없는 로그인 성공",
                                            summary = "기존 회원 로그인 성공, 추가 정보 필요 없음",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                        "user": {
                            "name": "홍길동",
                            "role": "USER",
                            "bodyTypeId": 2
                        },
                        "additionalInfoNeeded": null
                    }
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "추가 정보가 필요한 로그인 성공",
                                            summary = "신규 회원 로그인 성공, 추가 정보 필요",
                                            value = """
                {
                    "timestamp": "2025-06-29T12:35:00.123",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": null,
                        "refreshToken": null,
                        "user": null,
                        "additionalInfoNeeded": {
                            "email": "newuser@example.com",
                            "name": "신규 회원"
                        }
                    }
                }
                """
                                    )
                            }
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
                    responseCode = "502",
                    description = "카카오 토큰/사용자 정보 요청 실패 관련 에러들",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "카카오 토큰 요청 실패",
                                            summary = "",
                                            value = """
            {
                "timestamp": "2025-06-29T12:40:00.000",
                "code": "KAKAO-502-001",
                "message": "카카오 토큰 요청에 실패했습니다."
            }
            """),
                                    @ExampleObject(
                                            name = "카카오 사용자 정보 요청 실패",
                                            summary = "",
                                            value = """
            {
                "timestamp": "2025-06-29T12:40:01.000",
                "code": "KAKAO-502-002",
                "message": "카카오 사용자 정보 요청에 실패했습니다."
            }
            """)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "카카오 서버와의 통신 불가능",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "카카오 서버와 통신 불가능",
                                    summary = "",
                                    value = """
            {
                "timestamp": "2025-06-29T12:40:00.000",
                "code": "KAKAO-503-001",
                "message": "카카오 서버와의 통신이 불가능합니다."
            }
            """)
                    )
            )
    })
    @PostMapping("/oauth2/kakao/login")
    public ResponseEntity<BaseResponse<AuthResponseDto>> loginWithKakao(
            @Valid @RequestBody KakaoLoginRequestDto request,
            HttpServletResponse servletResponse
    ) {
        AuthResponseDto authResponseDto = authService.processKakaoLogin(request);
        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 카카오 회원가입 완료 처리.
     * <p>
     * 클라이언트로부터 추가 프로필 정보가 포함된 가입 요청을 받아
     * 신규 회원으로 사용자 등록 및 인증 토큰을 발급.
     *
     * @param request KakaoSignupRequestDto - 카카오 회원가입 요청 DTO (추가 프로필 정보 포함)
     * @return 회원가입 완료 후 발급된 인증 토큰과 사용자 정보가 포함된 응답 반환
     *
     */
    @Operation(summary = "카카오 회원가입 완료 처리", description = "추가 프로필 정보가 포함된 가입 요청을 받아 신규 회원으로 사용자 등록 및 인증 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    summary = "회원가입 후 발급된 토큰과 유저 정보 반환",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                        "user": {
                            "name": "홍길동",
                            "role": "USER",
                            "bodyTypeId": 2
                        },
                        "additionalInfoNeeded": null
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
            )
    })
    @PostMapping("/oauth2/kakao/signup/complete")
    public ResponseEntity<BaseResponse<AuthResponseDto>> completeSignupWithKakao(
            @Valid @RequestBody KakaoSignupRequestDto request
    ) {
        AuthResponseDto responseDto = authService.completeSignupWithKakao(request);
        return ResponseEntity.ok(BaseResponse.onSuccess(responseDto));
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 재발급 처리
     *
     * @param request RefreshTokenRequestDto - 리프레시 토큰 재발급 요청 DTO (리프레시 토큰 포함)
     * @return 새로 발급된 액세스 토큰 및 리프레시 토큰이 포함된 응답
     *
     */
    @Operation(summary = "액세스 토큰 재발급", description = "리프레시 토큰으로 액세스 토큰 재발급 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    summary = "회원가입 후 발급된 토큰과 유저 정보 반환",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                        "user": {
                            "name": "홍길동",
                            "role": "USER",
                            "bodyTypeId": 2
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
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        AuthResponseDto authResponseDto = authService.refreshAccessToken(request);
        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 액세스 토큰 재발급 API
     * <p>
     * - 리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급합니다.
     * - 유효한 리프레시 토큰이 필요하며, 유저 정보도 함께 반환됩니다.
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
                  "message": "활성화된 세션이 존재하지 않습니다.",
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
                  "message": "로그아웃 처리 중 오류가 발생했습니다.",
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<UserIdResponseDto>> logout(@CurrentUser PrincipalDetails principal) {
        UserIdResponseDto userIdResponseDto = authService.logout(principal);
        return ResponseEntity.ok(BaseResponse.onSuccess(userIdResponseDto));
    }

    // 유저 프로필 변경(이름, 전화번호, 키, 몸무게, 체형타입을 바꿀 수 있음
    // 처음에는 기본 정보를 불러오고 그 이후에 변경가능 한 것

}