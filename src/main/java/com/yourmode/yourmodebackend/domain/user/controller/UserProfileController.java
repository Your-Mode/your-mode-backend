package com.yourmode.yourmodebackend.domain.user.controller;

import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserProfileResponseDto;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import com.yourmode.yourmodebackend.domain.user.service.UserProfileService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "UserProfile: 유저 프로필", description = "유저 프로필 조회 및 수정 API")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     * @param principal 인증된 사용자 정보
     * @return 프로필 정보 응답
     */
    @Operation(summary = "유저 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "프로필 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "프로필 조회 성공 예시",
                    summary = "프로필 정보 반환",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "userId": 1,
                            "name": "홍길동",
                            "phoneNumber": "01012345678",
                            "height": 175.5,
                            "weight": 68.2,
                            "gender": "MALE",
                            "bodyTypeId": 1,
                            "bodyTypeName": "스트레이트"
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "접근 권한 없음",
                    summary = "인증 정보가 없거나 접근 권한이 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-403-001",
                        "message": "접근 권한이 없습니다. 인증 정보를 확인해주세요."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로필 또는 사용자 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "프로필 없음",
                    summary = "프로필 정보가 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-404-004",
                        "message": "사용자 프로필을 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileResponseDto>> getMyProfile(@CurrentUser PrincipalDetails principal) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                userProfileService.getMyProfile(principal.getUserId())
        ));
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * @param principal 인증된 사용자 정보
     * @param request 프로필 수정 요청 DTO
     * @return 수정된 프로필 정보 응답
     */
    @Operation(summary = "유저 프로필 수정", description = "이름, 전화번호, 키, 몸무게, 성별, 체형타입을 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "프로필 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "프로필 수정 성공 예시",
                    summary = "수정된 프로필 정보 반환",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "userId": 1,
                            "name": "홍길동",
                            "phoneNumber": "01012345678",
                            "height": 180.0,
                            "weight": 70.0,
                            "gender": "MALE",
                            "bodyTypeId": 2,
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "접근 권한 없음",
                    summary = "인증 정보가 없거나 접근 권한이 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-403-001",
                        "message": "접근 권한이 없습니다. 인증 정보를 확인해주세요."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "프로필 또는 사용자 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "프로필 없음",
                    summary = "프로필 정보가 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-404-004",
                        "message": "사용자 프로필을 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/me")
    public ResponseEntity<BaseResponse<UserProfileResponseDto>> updateMyProfile(
            @CurrentUser PrincipalDetails principal,
            @RequestBody @Valid UserProfileUpdateRequestDto request
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                userProfileService.updateMyProfile(principal.getUserId(), request)
        ));
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자가 자신의 비밀번호를 변경합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "비밀번호 변경 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "비밀번호 변경 성공 예시",
                    summary = "비밀번호가 성공적으로 변경된 경우",
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
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "접근 권한 없음",
                    summary = "인증 정보가 없거나 접근 권한이 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-403-001",
                        "message": "접근 권한이 없습니다. 인증 정보를 확인해주세요."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청(비밀번호 형식 오류 등)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "비밀번호 형식 오류",
                    summary = "비밀번호가 정책에 맞지 않는 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:36:00.000",
                        "code": "AUTH-400-001",
                        "message": "비밀번호는 최소 8자 이상이어야 합니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패(로그인 정보 없음)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    summary = "로그인 정보가 없거나 세션이 만료된 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:37:00.000",
                        "code": "AUTH-401-001",
                        "message": "인증이 필요합니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "사용자 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "사용자 없음",
                    summary = "로그인 정보가 잘못된 경우",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:35:00.000",
                        "code": "AUTH-404-001",
                        "message": "해당 이메일의 사용자를 찾을 수 없습니다."
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
                    summary = "비밀번호 변경 처리 중 서버 오류 발생",
                    value = """
                    {
                        "timestamp": "2025-06-29T12:38:00.000",
                        "code": "AUTH-500-001",
                        "message": "비밀번호 변경 처리 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/me/password")
    public ResponseEntity<BaseResponse<String>> updatePassword(
            @CurrentUser PrincipalDetails principal,
            @RequestBody @Valid PasswordUpdateRequestDto request
    ) {
        userProfileService.updatePassword(principal.getUserId(), request.getNewPassword());
        return ResponseEntity.ok(BaseResponse.onSuccess("비밀번호가 성공적으로 변경되었습니다."));
    }
}

