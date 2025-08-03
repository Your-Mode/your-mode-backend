package com.yourmode.yourmodebackend.domain.content.controller;

import com.yourmode.yourmodebackend.domain.content.dto.request.ContentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.service.ContentService;
import com.yourmode.yourmodebackend.global.config.security.auth.CurrentUser;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "Contents: 생성, 수정, 조회 API", description = "Content 자동 생성, 수정, 조회 API")
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
    @Operation(
        summary = "컨텐츠 생성",
        description = "새로운 컨텐츠를 생성합니다. mainImgUrl과 block의 imageUrl에는 S3에 업로드된 파일의 URL을 입력해야 합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ContentCreateRequestDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "ContentCreateRequest",
                    summary = "컨텐츠 생성 예시",
                    value = """
{
  "title": "2024 S/S 트렌드 분석",
  "mainImgUrl": "https://your-bucket.s3.amazonaws.com/main-image.jpg",
  "isRecommended": true,
  "publishAt": "2025-08-01T10:00:00",
  "contentsRequestId": 1,
  "categoryIds": [1, 2],
  "bodyTypeIds": [1, 3],
  "blocks": [
    {
      "blockType": 2,
      "contentData": "이번 시즌 주요 트렌드는...",
      "blockOrder": 1,
      "style": {
        "fontFamily": "Noto Sans",
        "fontSize": 16,
        "fontWeight": "bold",
        "textColor": "#222222",
        "backgroundColor": "#ffffff",
        "textAlign": "left"
      },
      "images": []
    },
    {
      "blockType": 1,
      "contentData": "",
      "blockOrder": 2,
      "style": null,
      "images": [
        {
          "imageUrl": "https://your-bucket.s3.amazonaws.com/image1.jpg",
          "imageOrder": 1
        }
      ]
    }
  ]
}
"""
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentDetailResponseDto.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ContentDetailResponseDto> createContent(
            @RequestBody ContentCreateRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(contentService.createContent(dto, userDetails.getUserId()));
    }

    @PutMapping("/{contentId}")
    @Operation(
        summary = "컨텐츠 수정",
        description = "기존 컨텐츠를 수정합니다. 컨텐츠 소유자만 수정할 수 있습니다. mainImgUrl과 block의 imageUrl에는 S3에 업로드된 파일의 URL을 입력해야 합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ContentCreateRequestDto.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    name = "ContentUpdateRequest",
                    summary = "컨텐츠 수정 예시",
                    value = """
{
  "title": "2024 S/S 트렌드 분석 (수정)",
  "mainImgUrl": "https://your-bucket.s3.amazonaws.com/updated-main-image.jpg",
  "isRecommended": false,
  "publishAt": "2025-08-15T10:00:00",
  "contentsRequestId": 1,
  "categoryIds": [1, 3],
  "bodyTypeIds": [2, 4],
  "blocks": [
    {
      "blockType": 2,
      "contentData": "수정된 시즌 트렌드 분석...",
      "blockOrder": 1,
      "style": {
        "fontFamily": "Noto Sans",
        "fontSize": 18,
        "fontWeight": "normal",
        "textColor": "#333333",
        "backgroundColor": "#f8f9fa",
        "textAlign": "center"
      },
      "images": []
    }
  ]
}
"""
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentDetailResponseDto.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (컨텐츠 소유자가 아님)"),
        @ApiResponse(responseCode = "404", description = "컨텐츠를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ContentDetailResponseDto> updateContent(
            @Parameter(
                description = "수정할 컨텐츠 ID",
                example = "1",
                required = true
            )
            @PathVariable Integer contentId,
            @Parameter(
                description = "컨텐츠 수정 요청 데이터",
                required = true
            )
            @RequestBody ContentCreateRequestDto dto,
            @CurrentUser PrincipalDetails userDetails
    ) {
        return ResponseEntity.ok(contentService.updateContent(contentId, dto, userDetails.getUserId()));
    }

    @GetMapping("/{contentId}")
    @Operation(
        summary = "컨텐츠 상세 조회",
        description = "특정 컨텐츠의 상세 정보를 조회합니다. 컨텐츠 정보, 카테고리, 바디타입, 블록 정보를 모두 포함합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentDetailResponseDto.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "컨텐츠를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<ContentDetailResponseDto> getContentDetail(
            @Parameter(
                description = "조회할 컨텐츠 ID",
                example = "1",
                required = true
            )
            @PathVariable Integer contentId
    ) {
        return ResponseEntity.ok(contentService.getContentDetail(contentId));
    }

    @GetMapping
    @Operation(
        summary = "컨텐츠 목록 조회",
        description = "모든 컨텐츠의 목록을 조회합니다. 각 컨텐츠의 기본 정보와 카테고리, 바디타입 정보를 포함합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentListResponseDto.class)
            )
        ),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<List<ContentListResponseDto>> getAllContents() {
        return ResponseEntity.ok(contentService.getAllContents());
    }
} 