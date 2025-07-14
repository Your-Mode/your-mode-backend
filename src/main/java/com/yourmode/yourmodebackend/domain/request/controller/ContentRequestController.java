package com.yourmode.yourmodebackend.domain.request.controller;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestSummaryDto;
import com.yourmode.yourmodebackend.domain.request.service.ContentRequestService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/content-requests")
@RequiredArgsConstructor
@Tag(name = "Content-requests: 컨텐츠 요청 API", description = "컨텐츠 요청서 생성, 조회, 상태 변경 등")
public class ContentRequestController {

    private final ContentRequestService contentRequestService;

    @Operation(
            summary = "컨텐츠 요청 생성",
            description = "현재 로그인한 사용자가 새로운 컨텐츠 요청서를 작성하여 서버에 저장합니다."
    )
    @PostMapping
    public ResponseEntity<BaseResponse<ContentRequestResponseDto>> createContentRequest(
            @Valid @RequestBody ContentRequestCreateDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        ContentRequestResponseDto savedRequest = contentRequestService.createContentRequest(dto, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(savedRequest));
    }

    @Operation(
            summary = "사용자별 컨텐츠 요청 조회",
            description = "현재 로그인한 사용자가 작성한 모든 컨텐츠 요청서 목록을 조회합니다."
    )
    @GetMapping("/my-requests")
    public ResponseEntity<BaseResponse<List<ContentRequestSummaryDto>>> getMyRequests(
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<ContentRequestSummaryDto> requestSummaryDto = contentRequestService.getRequestsByUserId(userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestSummaryDto));
    }

    @Operation(
            summary = "컨텐츠 요청 상세 조회",
            description = "컨텐츠 요청 ID로 단일 컨텐츠 요청서를 조회합니다."
    )
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<BaseResponse<ContentRequestDetailDto>> getContentRequestById(
            @PathVariable Integer id,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        ContentRequestDetailDto requestDetailDto = contentRequestService.getContentRequestById(id, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestDetailDto));
    }

    // ===================================== editor =====================================

    // 에디터 배정
    // 1. 작성 시 배정되는 것인지
    // 2. 별도로 지정해주는 단계 추가

    /**
     * 에디터 전용 API: 모든 컨텐츠 요청 목록을 조회
     * - 관리자나 에디터 권한을 가진 사용자만 접근 가능
     * - 전체 요청 목록을 필터링/정렬 옵션과 함께 조회할 수 있음 (예: 상태, 공개 여부 등)
     */
    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<ContentRequestSummaryDto>>> getAllRequestsForEditor() {
        // TODO: 에디터 권한 확인 및 전체 요청 리스트 조회 구현
        return null;
    }

    /**
     * 에디터 전용 API: 특정 컨텐츠 요청 상세 조회
     * - 에디터가 사용자의 요청 상세 정보를 확인할 수 있도록 제공
     * - 사용자 프로필, 요청 상태 이력 등 포함
     */
    @GetMapping("/all/{id}")
    public ResponseEntity<BaseResponse<ContentRequestDetailDto>> getRequestDetailForEditor(@PathVariable Integer id) {
        // TODO: 에디터 권한 확인 및 요청 상세 정보 조회 구현
        return null;
    }

    /**
     * 에디터 전용 API: 요청 상태 변경
     * - 에디터가 해당 요청의 상태를 업데이트
     * - 상태 변경 이력에 기록됨 (변경 시점, 에디터 ID 등)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse<Void>> updateRequestStatus(
            @PathVariable Integer id,
            @RequestParam String statusCode,
            @CurrentUser PrincipalDetails userDetails
    ) {
        // TODO: 에디터 권한 확인 및 상태 변경 로직 구현
        return null;
    }




}
