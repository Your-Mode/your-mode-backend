package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.request.PasswordChangeRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsSendRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsVerifyRequestDto;
import com.yourmode.yourmodebackend.domain.user.service.PasswordResetServiceImpl;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
@Tag(name = "Auth: 비밀번호 재설정", description = "SMS 인증 및 비밀번호 변경 관련 API")
public class PasswordResetController {
    private final PasswordResetServiceImpl passwordResetService;

    /**
     * SMS 인증 코드 전송
     *
     * @param request SmsSendRequestDto - 전화번호를 포함한 인증 코드 전송 요청 DTO
     * @return 문자 전송 성공 메시지를 담은 응답
     */
    @Operation(summary = "SMS 인증코드 전송", description = "입력된 전화번호로 SMS 인증 코드를 전송합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "SMS 인증코드 전송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "인증코드 전송 성공 예시",
                                    summary = "SMS 인증코드가 정상적으로 전송된 경우",
                                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789Z",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": "SMS 인증코드가 성공적으로 전송되었습니다."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전화번호로 가입된 사용자 미존재",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "전화번호로 가입된 사용자가 없을 시",
                                    summary = "존재하지 않는 번호로 요청한 경우",
                                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789Z",
                        "code": "AUTH-404-002",
                        "message": "해당 전화번호로 가입된 사용자가 존재하지 않습니다."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "SMS 인증코드 발송 횟수 제한 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "SMS 발송 횟수 제한 초과 시",
                                    summary = "지정된 시간 내 발송 요청이 과도한 경우",
                                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789Z",
                        "code": "AUTH-429-001",
                        "message": "SMS 발송 요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요."
                    }
                    """
                            )

                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "SMS 인증 코드 전송 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "SMS 인증 코드 전송 실패",
                                    summary = "외부 문자 발송 API 실패 등 내부 오류 발생 시",
                                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789Z",
                        "code": "AUTH-500-006",
                        "message": "SMS 인증 코드 전송 중 오류가 발생했습니다."
                    }
                    """
                            )
                    )
            )
    })
    @PostMapping("/send-code")
    public ResponseEntity<BaseResponse<String>> sendSMS(@Valid @RequestBody SmsSendRequestDto request) {
        passwordResetService.sendSMS(request);
        return ResponseEntity.ok(BaseResponse.onSuccess("SMS 인증코드가 성공적으로 전송되었습니다."));
    }

    /**
     * SMS 인증 코드 검증
     *
     * @param request SmsVerifyRequestDto - 전화번호와 인증 코드를 포함한 검증 요청 DTO
     * @return 인증 성공 메시지를 담은 응답
     */
    @Operation(summary = "SMS 인증코드 검증", description = "입력된 전화번호와 인증 코드를 검증합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "SMS 인증코드 인증 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "인증코드 인증 성공 예시",
                                    summary = "사용자가 올바른 인증코드를 입력한 경우",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": "SMS 인증에 성공했습니다."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "SMS 인증코드 인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "SMS 인증코드 인증 실패 예시",
                                    summary = "잘못된 인증코드 또는 만료된 인증코드를 입력한 경우",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "AUTH-400-002",
                    "message": "인증 코드가 유효하지 않습니다."
                }
                """
                            )
                    )
            )
    })
    @PostMapping("/verify-code")
    public ResponseEntity<BaseResponse<String>> verifyCode(@Valid @RequestBody SmsVerifyRequestDto request) {
        passwordResetService.verifyCode(request); // 인증 실패 시 예외 던짐
        return ResponseEntity.ok(BaseResponse.onSuccess("SMS 인증에 성공했습니다."));
    }

    /**
     * 비밀번호 변경
     *
     * @param request PasswordChangeRequestDto - 전화번호, 새 비밀번호 등을 포함한 비밀번호 변경 요청 DTO
     * @return 비밀번호 변경 성공 메시지를 담은 응답
     */
    @Operation(summary = "비밀번호 변경", description = "인증이 완료된 사용자에 대해 새 비밀번호로 변경합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 변경 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "비밀번호 변경 성공 예시",
                                    summary = "SMS 인증을 완료한 후 새 비밀번호로 변경에 성공한 경우",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "COMMON200",
                    "message": "요청에 성공하였습니다.",
                    "result": "비밀번호가 성공적으로 변경되었습니다."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 변경 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "비밀번호 변경 권한이 없을 시",
                                    summary = "SMS 인증을 하지 않고 비밀번호 변경 요청한 경우",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "AUTH-401-004",
                    "message": "비밀번호 변경 권한이 없습니다. 인증을 먼저 진행해주세요."
                }
                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전화번호로 가입된 사용자 미존재",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "전화번호로 가입된 사용자가 없을 시",
                                    summary = "존재하지 않는 번호로 요청한 경우",
                                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789Z",
                        "code": "AUTH-404-002",
                        "message": "해당 전화번호로 가입된 사용자가 존재하지 않습니다."
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "비밀번호 변경 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "DB Update 오류 - User",
                                    summary = "DB 연결 오류, 비밀번호 암호화 실패 등 내부 문제 발생 시",
                                    value = """
                {
                    "timestamp": "2025-06-29T12:34:56.789",
                    "code": "AUTH-500-007",
                    "message": "비밀번호 변경에 실패했습니다."
                }
                """
                            )
                    )
            )
    })
    @PutMapping("/change-password")
    public ResponseEntity<BaseResponse<String>> changePassword(@Valid @RequestBody PasswordChangeRequestDto request) {
        passwordResetService.changePassword(request);
        return ResponseEntity.ok(BaseResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다."));
    }

}
