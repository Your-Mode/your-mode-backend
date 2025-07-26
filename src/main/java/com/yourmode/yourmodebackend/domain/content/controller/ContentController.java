package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.request.ContentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentService;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

    /**
     * [이미지 업로드 절차 안내]
     * 1. /api/content/s3/presigned-url?fileName=contents/uuid.jpg 호출 → presigned URL 발급
     * 2. 프론트엔드에서 해당 presigned URL로 PUT 요청하여 S3에 직접 이미지 업로드
     * 3. 업로드된 S3 URL(ex: https://bucket.s3.amazonaws.com/contents/uuid.jpg)을 ContentCreateRequestDto.mainImgUrl, block imageUrl 등에 세팅하여 Content 생성 API 호출
     */

    // Content 생성 API는 mainImgUrl, block imageUrl에 S3 URL만 받음
    @PostMapping
    public ResponseEntity<ContentDetailResponseDto> createContent(
            @RequestBody ContentCreateRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(contentService.createContent(dto, userDetails.getUserId()));
    }

    @PutMapping("/{contentId}")
    public ResponseEntity<ContentDetailResponseDto> updateContent(
            @PathVariable Integer contentId,
            @RequestBody ContentCreateRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(contentService.updateContent(contentId, dto, userDetails.getUserId()));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<ContentDetailResponseDto> getContentDetail(@PathVariable Integer contentId) {
        return ResponseEntity.ok(contentService.getContentDetail(contentId));
    }

    @GetMapping
    public ResponseEntity<List<ContentListResponseDto>> getAllContents() {
        return ResponseEntity.ok(contentService.getAllContents());
    }
} 