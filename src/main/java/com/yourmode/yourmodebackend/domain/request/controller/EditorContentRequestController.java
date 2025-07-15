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

    @Operation(summary = "모든 컨텐츠 요청 목록 조회", description = "에디터/관리자 권한으로 전체 요청 목록을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<EditorContentRequestSummaryDto>>> getAllRequestsForEditor(
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        List<EditorContentRequestSummaryDto> requestList = editorContentRequestService.getAllRequestsForEditor();
        return ResponseEntity.ok(BaseResponse.onSuccess(requestList));
    }

    @Operation(summary = "특정 컨텐츠 요청 상세 조회", description = "에디터가 사용자의 요청 상세 정보를 확인합니다.")
    @GetMapping("/all/{id}")
    public ResponseEntity<BaseResponse<EditorContentRequestDetailDto>> getRequestDetailForEditor(
            @PathVariable Long id,
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        EditorContentRequestDetailDto detailDto = editorContentRequestService.getContentRequestDetailForEditor(id);
        return ResponseEntity.ok(BaseResponse.onSuccess(detailDto));
    }

    @Operation(summary = "요청 상태 변경", description = "에디터가 요청의 상태를 변경합니다.")
    @PostMapping("/all/{id}/status")
    public ResponseEntity<BaseResponse<Void>> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String statusCode,
            @CurrentUser PrincipalDetails userDetails
    ) {
        validateAdminAccess(userDetails);
        editorContentRequestService.updateStatus(id, statusCode, userDetails.getUserId());
        return ResponseEntity.ok(BaseResponse.onSuccess(null));
    }

    private void validateAdminAccess(PrincipalDetails userDetails) {
        if (userDetails.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("관리자만 접근할 수 있습니다.");
        }
    }
} 