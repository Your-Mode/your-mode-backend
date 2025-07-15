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

@RestController
@RequestMapping("/api/content-requests")
@RequiredArgsConstructor
@Tag(name = "Content-requests: 사용자용 API", description = "컨텐츠 요청서 생성, 조회 등 사용자 기능")
public class UserContentRequestController {
    private final UserContentRequestService userContentRequestService;

    @Operation(summary = "컨텐츠 요청 생성", description = "로그인한 사용자가 새로운 컨텐츠 요청서를 작성하여 서버에 저장합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<ContentRequestResponseDto>> createContentRequest(
            @Valid @RequestBody ContentRequestCreateDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        ContentRequestResponseDto savedRequest = userContentRequestService.createContentRequest(dto, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(savedRequest));
    }

    @Operation(summary = "사용자별 컨텐츠 요청 조회", description = "로그인한 사용자가 작성한 모든 컨텐츠 요청서 목록을 조회합니다.")
    @GetMapping("/my-requests")
    public ResponseEntity<BaseResponse<List<UserContentRequestSummaryDto>>> getMyRequests(
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        List<UserContentRequestSummaryDto> requestSummaryDto = userContentRequestService.getRequestsByUserId(userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestSummaryDto));
    }

    @Operation(summary = "컨텐츠 요청 상세 조회", description = "컨텐츠 요청 ID로 단일 컨텐츠 요청서를 조회합니다.")
    @GetMapping("/my-requests/{id}")
    public ResponseEntity<BaseResponse<UserContentRequestDetailDto>> getContentRequestById(
            @PathVariable Long id,
            @CurrentUser PrincipalDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        UserContentRequestDetailDto requestDetailDto = userContentRequestService.getContentRequestById(id, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(requestDetailDto));
    }
} 