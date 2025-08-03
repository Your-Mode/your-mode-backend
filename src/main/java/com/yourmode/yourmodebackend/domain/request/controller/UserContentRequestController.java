package com.yourmode.yourmodebackend.domain.request.controller;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.service.UserContentRequestService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/content-requests")
@RequiredArgsConstructor
@Tag(name = "Content-requests: 사용자용 API", description = "컨텐츠 요청서 생성, 조회 등 사용자 기능")
public class UserContentRequestController {
    private final UserContentRequestService userContentRequestService;

    @Operation(summary = "콘텐츠 요청 생성", description = "로그인한 사용자가 새로운 콘텐츠 요청서를 작성하여 서버에 저장합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "콘텐츠 요청 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 생성 성공 예시",
                    summary = "요청이 성공적으로 생성된 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": { /* ... */ }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "요청 또는 카테고리 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 없음",
                    summary = "요청을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-404-001",
                        "message": "해당 콘텐츠 신청을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "카테고리 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "카테고리 없음",
                    summary = "카테고리를 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-404-002",
                        "message": "해당 카테고리를 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 파라미터",
                    summary = "파라미터가 유효하지 않은 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-400-002",
                        "message": "요청 파라미터가 유효하지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 신청 상태",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "유효하지 않은 상태",
                    summary = "신청 상태가 유효하지 않은 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-400-001",
                        "message": "유효하지 않은 신청 상태입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "권한 없음",
                    summary = "해당 요청에 대한 접근 권한이 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-403-001",
                        "message": "해당 요청에 대한 접근 권한이 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "중복 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "중복 요청",
                    summary = "이미 신청된 콘텐츠",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-409-001",
                        "message": "이미 신청된 콘텐츠입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 처리된 요청",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "이미 처리됨",
                    summary = "이미 처리된 요청",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-409-002",
                        "message": "이미 처리된 요청입니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "DB 저장 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "DB 저장 오류",
                    summary = "DB 저장 중 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-500-001",
                        "message": "콘텐츠 신청 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "DB 수정 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "DB 수정 오류",
                    summary = "DB 수정 중 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-500-002",
                        "message": "콘텐츠 신청 정보 수정 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "DB 삭제 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "DB 삭제 오류",
                    summary = "DB 삭제 중 오류",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-500-003",
                        "message": "콘텐츠 신청 정보 삭제 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<BaseResponse<ContentRequestResponseDto>> createContentRequest(
            @Valid @RequestBody ContentRequestCreateDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Integer userId = userDetails.getUserId();
        ContentRequestResponseDto savedRequest = userContentRequestService.createContentRequest(dto, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(savedRequest));
    }

    @Operation(summary = "사용자별 콘텐츠 요청 조회", description = "로그인한 사용자가 작성한 모든 콘텐츠 요청서 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "콘텐츠 요청 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 목록 조회 성공 예시",
                    summary = "요청 목록이 성공적으로 반환된 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": [ /* ... */ ]
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "요청 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 없음",
                    summary = "요청을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-404-001",
                        "message": "해당 콘텐츠 신청을 찾을 수 없습니다."
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
                    summary = "서버 내부 오류 발생",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-500-001",
                        "message": "콘텐츠 신청 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<List<UserContentRequestSummaryDto>>> getMyRequests(
            @CurrentUser PrincipalDetails userDetails
    ) {
        Integer userId = userDetails.getUserId();
        List<UserContentRequestSummaryDto> requestSummaryDto = userContentRequestService.getRequestsByUserId(userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestSummaryDto));
    }

    @Operation(summary = "콘텐츠 요청 상세 조회", description = "콘텐츠 요청 ID로 단일 콘텐츠 요청서를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "콘텐츠 요청 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 상세 조회 성공 예시",
                    summary = "요청 상세 정보가 성공적으로 반환된 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": { /* ... */ }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "요청 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 없음",
                    summary = "요청을 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-404-001",
                        "message": "해당 콘텐츠 신청을 찾을 수 없습니다."
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
                    summary = "서버 내부 오류 발생",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-500-001",
                        "message": "콘텐츠 신청 정보를 DB에 저장하는 중 오류가 발생했습니다."
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/me/{id}")
    public ResponseEntity<BaseResponse<UserContentRequestDetailDto>> getContentRequestById(
            @PathVariable Integer id,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Integer userId = userDetails.getUserId();
        UserContentRequestDetailDto requestDetailDto = userContentRequestService.getContentRequestById(id, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestDetailDto));
    }
} 