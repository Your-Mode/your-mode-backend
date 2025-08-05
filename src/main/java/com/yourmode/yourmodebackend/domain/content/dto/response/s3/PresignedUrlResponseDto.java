package com.yourmode.yourmodebackend.domain.content.dto.response.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Presigned URL 발급 응답")
public class PresignedUrlResponseDto {
    
    @Schema(
        description = "발급된 Presigned URL",
        example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=..."
    )
    private String presignedUrl;
    
    @Schema(
        description = "파일명",
        example = "test-image.jpg"
    )
    private String fileName;
    
    @Schema(
        description = "만료 시간(분)",
        example = "10"
    )
    private Integer expirationMinutes;
} 