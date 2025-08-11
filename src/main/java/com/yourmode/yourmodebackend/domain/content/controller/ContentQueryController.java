package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Tag(name = "Contents: 조회 API", description = "Content 목록/상세 조회 API")
public class ContentQueryController {

    private final ContentQueryService contentQueryService;

    @GetMapping
    @Operation(summary = "컨텐츠 목록 조회", description = "카테고리와 바디타입 필터, 페이지네이션 지원")
    public ResponseEntity<Page<ContentListResponseDto>> getContents(
            @RequestParam(required = false) List<Integer> categoryIds,
            @RequestParam(required = false) List<Integer> bodyTypeIds,
            @PageableDefault(size = 10, sort = {"createdAt"}) Pageable pageable
    ) {
        return ResponseEntity.ok(contentQueryService.getContents(categoryIds, bodyTypeIds, pageable));
    }

    @GetMapping("/{contentId}")
    @Operation(summary = "컨텐츠 상세 조회", description = "특정 컨텐츠 상세 정보 조회")
    public ResponseEntity<ContentDetailResponseDto> getContentDetail(@PathVariable Integer contentId) {
        return ResponseEntity.ok(contentQueryService.getContentDetail(contentId));
    }
}


