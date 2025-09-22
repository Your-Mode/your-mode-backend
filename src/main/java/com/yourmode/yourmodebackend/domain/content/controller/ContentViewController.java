package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewCountResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.UserViewCountResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentViewService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Contents: 조회수 API", description = "Content 조회수 관련 API")
public class ContentViewController {

    private final ContentViewService contentViewService;

    /**
     * 콘텐츠에 조회수를 추가합니다.
     * @param contentId 콘텐츠 ID
     * @param userDetails 인증된 사용자 정보
     * @return 조회수 추가 결과
     */
    @PostMapping("/{contentId}/view")
    @Operation(summary = "조회수 추가", description = "특정 콘텐츠에 조회수를 추가합니다. 이미 조회한 경우 조회 시간을 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회수 추가 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회수 추가 성공 예시",
                    summary = "조회수가 성공적으로 추가된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "userId": 1,
                            "contentId": 1,
                            "viewedAt": "2025-01-15T12:34:56.789",
                            "message": "조회수가 성공적으로 추가되었습니다."
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "콘텐츠를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "콘텐츠 없음",
                    summary = "존재하지 않는 콘텐츠에 조회수를 추가하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "CONTENT-404-001",
                        "message": "콘텐츠를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    summary = "로그인하지 않은 사용자가 조회수를 추가하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "AUTH-401-001",
                        "message": "인증이 필요합니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<ContentViewResponseDto>> addContentView(
            @Parameter(description = "콘텐츠 ID", example = "1")
            @PathVariable Integer contentId,
            @CurrentUser PrincipalDetails userDetails
    ) {
        ContentViewResponseDto response = contentViewService.addOrUpdateView(contentId, userDetails.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    /**
     * 특정 콘텐츠의 조회수를 조회합니다.
     * @param contentId 콘텐츠 ID
     * @return 조회수
     */
    @GetMapping("/{contentId}/view-count")
    @Operation(summary = "조회수 조회", description = "특정 콘텐츠의 조회수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회수 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회수 조회 성공 예시",
                    summary = "조회수가 성공적으로 조회된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "contentId": 1,
                            "viewCount": 42
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "콘텐츠를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "콘텐츠 없음",
                    summary = "존재하지 않는 콘텐츠의 조회수를 조회하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "CONTENT-404-001",
                        "message": "콘텐츠를 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<ContentViewCountResponseDto>> getContentViewCount(
            @Parameter(description = "콘텐츠 ID", example = "1")
            @PathVariable Integer contentId
    ) {
        ContentViewCountResponseDto response = contentViewService.getViewCount(contentId);
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }

    /**
     * 현재 로그인한 사용자가 조회한 콘텐츠 수를 조회합니다.
     * @param userDetails 인증된 사용자 정보
     * @return 사용자가 조회한 콘텐츠 수
     */
    @GetMapping("/my/view-count")
    @Operation(summary = "내가 조회한 콘텐츠 수 조회", description = "현재 로그인한 사용자가 조회한 콘텐츠 수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회한 콘텐츠 수 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "조회한 콘텐츠 수 조회 성공 예시",
                    summary = "사용자가 조회한 콘텐츠 수가 성공적으로 조회된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "userId": 1,
                            "viewedContentsCount": 15
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "인증 실패",
                    summary = "로그인하지 않은 사용자가 조회한 콘텐츠 수를 조회하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "AUTH-401-001",
                        "message": "인증이 필요합니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<BaseResponse<UserViewCountResponseDto>> getMyViewCount(
            @CurrentUser PrincipalDetails userDetails
    ) {
        UserViewCountResponseDto response = contentViewService.getUserViewCount(userDetails.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess(response));
    }
}
