package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.LocalSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.service.AuthService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API (회원가입, 로그인 등)")
public class AuthController {

    private final AuthService authService;

    /**
     * 로컬 회원가입
     */
    @Operation(summary = "로컬 회원가입 API", description = "이메일 및 비밀번호를 통해 로컬 회원가입 후 액세스 토큰과 유저 정보를 반환하는 API 입니다.")
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

    // todo: login, socialLogin, logout, accessToken 재발급, 비밀번호 바꾸기
}