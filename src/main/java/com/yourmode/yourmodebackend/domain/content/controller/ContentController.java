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
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;

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