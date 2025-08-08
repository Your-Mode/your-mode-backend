package com.yourmode.yourmodebackend.domain.content.dto.request.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Presigned URL 발급 요청")
public class PresignedUrlRequestDto {
    
    @Schema(
        description = "사용자 ID",
        example = "123",
        required = true
    )
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Integer userId;
    
    @Schema(
        description = "S3에 저장할 파일명 (확장자 포함)",
        example = "test-image.jpg",
        required = true
    )
    @NotBlank(message = "파일명은 필수입니다.")
    private String fileName;
    
    @Schema(
        description = "Presigned URL 만료 시간(분)",
        example = "10",
        defaultValue = "10"
    )
    @Min(value = 1, message = "만료 시간은 최소 1분이어야 합니다.")
    @Max(value = 60, message = "만료 시간은 최대 60분까지 가능합니다.")
    @Builder.Default
    private Integer expirationMinutes = 10;
} 