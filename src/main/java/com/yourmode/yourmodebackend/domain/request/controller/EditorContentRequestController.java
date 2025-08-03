package com.yourmode.yourmodebackend.domain.request.controller;

import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.editor.response.EditorContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.service.EditorContentRequestService;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content-requests/editor")
@RequiredArgsConstructor
@Tag(name = "Content-requests: 에디터용 API", description = "컨텐츠 요청서 에디터(관리자) 기능")
public class EditorContentRequestController {
    private final EditorContentRequestService editorContentRequestService;

    @Operation(summary = "모든 콘텐츠 요청 목록 조회", description = "관리자 권한으로 전체 요청 목록을 조회합니다.")
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
                        "result": [
                            {
                                "id": 1,
                                "userId": 123,
                                "userName": "홍길동",
                                "bodyFeature": "마른 체형",
                                "situation": "데이트",
                                "recommendedStyle": "캐주얼",
                                "avoidedStyle": "정장",
                                "budget": 500000,
                                "isPublic": true,
                                "status": "신청 접수",
                                "createdAt": "2025-07-17T12:00:00.000"
                            }
                        ]
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
    @GetMapping
    public ResponseEntity<BaseResponse<List<EditorContentRequestSummaryDto>>> getAllRequestsForEditor(
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        List<EditorContentRequestSummaryDto> requestList = editorContentRequestService.getAllRequestsForEditor();
        return ResponseEntity.ok(BaseResponse.onSuccess(requestList));
    }

    @Operation(summary = "특정 콘텐츠 요청 상세 조회", description = "에디터가 사용자의 요청 상세 정보를 확인합니다.")
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
                        "result": {
                            "id": 1,
                            "profile": {
                                "userId": 123,
                                "userName": "홍길동",
                                "email": "hong@example.com",
                                "phoneNumber": "010-1234-5678"
                            },
                            "bodyFeature": "마른 체형",
                            "situation": "데이트",
                            "recommendedStyle": "캐주얼",
                            "avoidedStyle": "정장",
                            "budget": 500000,
                            "isPublic": true,
                            "status": "신청 접수",
                            "itemCategoryIds": [1, 2, 3],
                            "itemCategoryNames": ["상의", "하의", "신발"],
                            "createdAt": "2025-07-17T12:00:00.000"
                        }
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
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<EditorContentRequestDetailDto>> getRequestDetailForEditor(
            @PathVariable Integer id,
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        EditorContentRequestDetailDto detailDto = editorContentRequestService.getContentRequestDetailForEditor(id);
        return ResponseEntity.ok(BaseResponse.onSuccess(detailDto));
    }

    @Operation(summary = "요청 상태 변경", description = "에디터가 요청의 상태를 변경합니다. statusId는 상태 코드의 ID를 입력하세요.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "요청 상태 변경 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "요청 상태 변경 성공 예시",
                    summary = "요청 상태가 성공적으로 변경된 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "COMMON200",
                        "message": "요청에 성공하였습니다.",
                        "result": null
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "요청 또는 상태 정보 없음",
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
            description = "상태 정보 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "상태 없음",
                    summary = "상태를 찾을 수 없는 경우",
                    value = """
                    {
                        "timestamp": "2025-07-17T12:00:00.000",
                        "code": "REQ-404-003",
                        "message": "신청 상태 변경 이력을 찾을 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 상태 코드",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BaseResponse.class),
                examples = @ExampleObject(
                    name = "잘못된 상태 코드",
                    summary = "상태 코드가 유효하지 않은 경우",
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
    @PutMapping("/{id}/status")
    public ResponseEntity<BaseResponse<Void>> updateRequestStatus(
            @PathVariable Integer id,
            @RequestParam Integer statusId,
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        editorContentRequestService.updateStatus(id, statusId, userDetails.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess(null));
    }

    private void validateAdminAccess(PrincipalDetails userDetails) {
        if (userDetails.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }
} 