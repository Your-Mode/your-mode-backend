package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.response.LikeResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentLikeService;
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
@Tag(name = "Contents: 콘텐츠 좋아요", description = "콘텐츠 좋아요 관련 API")
public class ContentLikeController {

    private final ContentLikeService contentLikeService;

    /**
     * 콘텐츠에 좋아요를 추가합니다.
     * @param contentId 콘텐츠 ID
     * @param principal 인증된 사용자 정보
     * @return 추가된 좋아요 정보
     */
    @Operation(summary = "좋아요 추가", description = "특정 콘텐츠에 좋아요를 추가합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "좋아요 추가 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "좋아요 추가 성공 예시",
                    summary = "좋아요가 성공적으로 추가된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "id": 1,
                            "createdAt": "2025-01-15T12:34:56.789",
                            "userId": 1,
                            "userName": "홍길동",
                            "contentId": 1,
                            "contentTitle": "2024 S/S 트렌드 분석"
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
                    summary = "존재하지 않는 콘텐츠에 좋아요를 추가하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-001",
                        "message": "해당 컨텐츠를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 좋아요를 누른 콘텐츠",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "중복 좋아요",
                    summary = "이미 좋아요를 누른 콘텐츠에 다시 좋아요를 누르는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-409-001",
                        "message": "이미 좋아요를 누른 콘텐츠입니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/{contentId}/likes")
    public ResponseEntity<BaseResponse<LikeResponseDto>> addLike(
            @Parameter(description = "콘텐츠 ID", example = "1") @PathVariable Integer contentId,
            @CurrentUser PrincipalDetails principal
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                contentLikeService.addLike(contentId, principal.getUserId())
        ));
    }

    /**
     * 콘텐츠의 좋아요를 제거합니다.
     * @param contentId 콘텐츠 ID
     * @param principal 인증된 사용자 정보
     * @return 제거 성공 메시지
     */
    @Operation(summary = "좋아요 제거", description = "특정 콘텐츠의 좋아요를 제거합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "좋아요 제거 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "좋아요 제거 성공 예시",
                    summary = "좋아요가 성공적으로 제거된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": "좋아요가 성공적으로 제거되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "콘텐츠 또는 좋아요를 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "좋아요 없음",
                    summary = "존재하지 않는 콘텐츠이거나 좋아요를 누르지 않은 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-007",
                        "message": "해당 좋아요를 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/{contentId}/likes")
    public ResponseEntity<BaseResponse<String>> removeLike(
            @Parameter(description = "콘텐츠 ID", example = "1") @PathVariable Integer contentId,
            @CurrentUser PrincipalDetails principal
    ) {
        contentLikeService.removeLike(contentId, principal.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess("좋아요가 성공적으로 제거되었습니다."));
    }


    /**
     * 내가 누른 좋아요 총 개수를 조회합니다.
     * @param principal 인증된 사용자 정보
     * @return 좋아요 총 개수
     */
    @Operation(summary = "내 좋아요 개수 조회", description = "현재 사용자가 누른 좋아요의 총 개수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "내 좋아요 개수 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "내 좋아요 개수 조회 성공 예시",
                    summary = "내 좋아요 개수가 성공적으로 조회된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": 25
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/my/likes/count")
    public ResponseEntity<BaseResponse<Long>> getMyLikesCount(
            @CurrentUser PrincipalDetails principal
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                contentLikeService.getMyLikesCount(principal.getUserId())
        ));
    }

}
