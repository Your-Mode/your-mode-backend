package com.yourmode.yourmodebackend.domain.content.dto.response.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "배치 Presigned URL 발급 응답")
public class BatchPresignedUrlResponseDto {
    
    @Schema(
        description = "파일명별 Presigned URL 맵",
        example = "{\"image1.jpg\": \"https://...\", \"image2.jpg\": \"https://...\"}"
    )
    private Map<String, String> presignedUrls;
    
    @Schema(
        description = "발급된 URL 개수",
        example = "3"
    )
    private Integer count;
    
    @Schema(
        description = "만료 시간(분)",
        example = "10"
    )
    private Integer expirationMinutes;
} 