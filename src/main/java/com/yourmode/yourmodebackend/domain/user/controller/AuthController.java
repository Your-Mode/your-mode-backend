package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.KakaoSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalLoginRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.service.AuthService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
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
@Tag(name = "Auth", description = "인증 관련 API (회원가입, 로그인 등)")
public class AuthController {

    private final AuthService authService;

    /**
     * 로컬 회원가입
     */
    @Operation(summary = "로컬 회원가입 API", description = "이메일, 비밀번호 등을 통해 로컬 회원가입 후 액세스 토큰과 유저 정보를 반환하는 API 입니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "COMMON200",
                    description = "회원가입 완료 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공",
                                    summary = "정상 처리된 응답 예시",
                                    value = """
                    {
                        "timestamp": "2025-06-23T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                            "email": "test@example.com"
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "COMMON402",
                    description = "잘못된 요청 (입력 값 오류)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원가입 실패 - Validation Error",
                                    summary = "필수 필드 누락 등 잘못된 입력값으로 인한 실패",
                                    value = """
                    {
                        "timestamp": "2025-06-23T12:35:00.123",
                        "code": "COMMON402",
                        "message": "Validation Error입니다.",
                        "result": {
                            "email": "이메일은 필수입니다."
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "AUTH409",
                    description = "중복된 이메일로 인한 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "회원가입 실패 - 이메일 중복",
                                    summary = "이미 등록된 이메일",
                                    value = """
                    {
                        "timestamp": "2025-06-23T12:35:10.456",
                        "code": "AUTH409",
                        "message": "이미 사용 중인 이메일입니다.",
                        "result": null
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
     */
    @Operation(summary = "로컬 로그인 API", description = "이메일, 비밀번호로 로그인 후 액세스 토큰과 유저 정보를 반환하는 API 입니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "COMMON200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공",
                                    summary = "정상 처리된 응답 예시",
                                    value = """
                    {
                        "timestamp": "2025-06-27T15:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                            "refreshToken": "dGhpc0lzUmVmcmVzaFRva2Vu...",
                            "email": "test@example.com"
                        }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "AUTH401",
                    description = "로그인 실패 (인증 정보 불일치)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로그인 실패 - 인증 실패",
                                    summary = "이메일 또는 비밀번호가 틀림",
                                    value = """
                    {
                        "timestamp": "2025-06-27T15:01:00.000",
                        "code": "AUTH401",
                        "message": "인증 정보가 올바르지 않습니다.",
                        "result": null
                    }
                    """
                            )
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
     * @param body            JSON으로 전달된 인가 코드 ({@code {"code":"…"}})
     * @param servletResponse 리프레시 토큰을 쿠키에 담기 위한 응답 객체
     * @return 이메일, 닉네임과 함께 Authorization 헤더에 액세스 토큰이 설정된 응답
     */
    @PostMapping("/oauth2/kakao/login")
    public ResponseEntity<BaseResponse<AuthResponseDto>> loginWithKakao(
            @RequestBody Map<String,String> body,
            HttpServletResponse servletResponse
    ) {
        String code = body.get("code");
        log.debug(code);

        AuthResponseDto authResponseDto = authService.processKakaoLogin(code);
        return ResponseEntity.ok(BaseResponse.onSuccess(authResponseDto));
    }

    /**
     * 카카오 회원가입 완료 처리.
     * <p>
     * 클라이언트로부터 추가 프로필 정보가 포함된 가입 요청을 받아
     * 신규 회원으로 사용자 등록 및 인증 토큰을 발급.
     */
    @PostMapping("/oauth2/kakao/signup/complete")
    public ResponseEntity<BaseResponse<AuthResponseDto>> completeSignupWithKakao(
            @RequestBody KakaoSignupRequestDto signupRequest
    ) {
        AuthResponseDto responseDto = authService.completeSignupWithKakao(signupRequest);
        return ResponseEntity.ok(BaseResponse.onSuccess(responseDto));
    }

    // todo: logout?, 탈퇴?, refresh token을 통한 access token 재발급
    // todo: 비밀번호 변경, 전화번호 변경, 유저 프로필 변경 등

}