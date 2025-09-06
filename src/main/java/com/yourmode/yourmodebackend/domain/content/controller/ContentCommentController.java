package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.request.CommentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.request.CommentUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentCommentService;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Contents: 콘텐츠 댓글", description = "콘텐츠 댓글 관련 API")
public class ContentCommentController {

    private final ContentCommentService contentCommentService;

    /**
     * 댓글을 작성합니다.
     * @param contentId 콘텐츠 ID
     * @param principal 인증된 사용자 정보
     * @param request 댓글 작성 요청 DTO
     * @return 작성된 댓글 정보
     */
    @Operation(summary = "댓글 작성", description = "특정 콘텐츠에 댓글을 작성합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 작성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 작성 성공 예시",
                    summary = "댓글이 성공적으로 작성된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "id": 1,
                            "commentText": "정말 유용한 정보네요!",
                            "createdAt": "2025-01-15T12:34:56.789",
                            "userId": 1,
                            "userName": "홍길동",
                            "contentId": 1
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
                    summary = "존재하지 않는 콘텐츠에 댓글을 작성하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-001",
                        "message": "해당 컨텐츠를 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/{contentId}/comments")
    public ResponseEntity<BaseResponse<CommentResponseDto>> createComment(
            @Parameter(description = "콘텐츠 ID", example = "1") @PathVariable Integer contentId,
            @CurrentUser PrincipalDetails principal,
            @RequestBody @Valid CommentCreateRequestDto request
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                contentCommentService.createComment(contentId, principal.getUserId(), request)
        ));
    }

    /**
     * 특정 콘텐츠의 댓글 목록을 조회합니다.
     * @param contentId 콘텐츠 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 댓글 목록과 페이지네이션 정보
     */
    @Operation(summary = "댓글 목록 조회", description = "특정 콘텐츠의 댓글 목록을 페이지네이션과 함께 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 목록 조회 성공 예시",
                    summary = "댓글 목록이 성공적으로 조회된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "comments": [
                                {
                                    "id": 1,
                                    "commentText": "정말 유용한 정보네요!",
                                    "createdAt": "2025-01-15T12:34:56.789",
                                    "userId": 1,
                                    "userName": "홍길동",
                                    "contentId": 1
                                }
                            ],
                            "totalCount": 1,
                            "currentPage": 0,
                            "totalPages": 1,
                            "hasNext": false
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
                    summary = "존재하지 않는 콘텐츠의 댓글을 조회하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-001",
                        "message": "해당 컨텐츠를 찾을 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{contentId}/comments")
    public ResponseEntity<BaseResponse<CommentListResponseDto>> getComments(
            @Parameter(description = "콘텐츠 ID", example = "1") @PathVariable Integer contentId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(BaseResponse.onSuccess(
                contentCommentService.getComments(contentId, pageable)
        ));
    }

    /**
     * 댓글을 수정합니다.
     * @param commentId 댓글 ID
     * @param principal 인증된 사용자 정보
     * @param request 댓글 수정 요청 DTO
     * @return 수정된 댓글 정보
     */
    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 수정 성공 예시",
                    summary = "댓글이 성공적으로 수정된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": {
                            "id": 1,
                            "commentText": "수정된 댓글 내용입니다.",
                            "createdAt": "2025-01-15T12:30:00.000",
                            "userId": 1,
                            "userName": "홍길동",
                            "contentId": 1
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "댓글을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 없음",
                    summary = "존재하지 않는 댓글을 수정하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-006",
                        "message": "해당 댓글을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "댓글 수정 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    summary = "본인이 작성하지 않은 댓글을 수정하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-403-002",
                        "message": "댓글에 대한 접근 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<BaseResponse<CommentResponseDto>> updateComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Integer commentId,
            @CurrentUser PrincipalDetails principal,
            @RequestBody @Valid CommentUpdateRequestDto request
    ) {
        return ResponseEntity.ok(BaseResponse.onSuccess(
                contentCommentService.updateComment(commentId, principal.getUserId(), request)
        ));
    }

    /**
     * 댓글을 삭제합니다.
     * @param commentId 댓글 ID
     * @param principal 인증된 사용자 정보
     * @return 삭제 성공 메시지
     */
    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "댓글 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 삭제 성공 예시",
                    summary = "댓글이 성공적으로 삭제된 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:34:56.789",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": "댓글이 성공적으로 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "댓글을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "댓글 없음",
                    summary = "존재하지 않는 댓글을 삭제하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-404-006",
                        "message": "해당 댓글을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "댓글 삭제 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    summary = "본인이 작성하지 않은 댓글을 삭제하려는 경우",
                    value = """
                    {
                        "timestamp": "2025-01-15T12:35:00.000",
                        "code": "CONT-403-002",
                        "message": "댓글에 대한 접근 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<BaseResponse<String>> deleteComment(
            @Parameter(description = "댓글 ID", example = "1") @PathVariable Integer commentId,
            @CurrentUser PrincipalDetails principal
    ) {
        contentCommentService.deleteComment(commentId, principal.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess("댓글이 성공적으로 삭제되었습니다."));
    }
}
