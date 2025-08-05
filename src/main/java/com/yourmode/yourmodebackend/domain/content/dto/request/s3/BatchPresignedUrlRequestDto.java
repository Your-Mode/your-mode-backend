package com.yourmode.yourmodebackend.domain.content.dto.request.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "배치 Presigned URL 발급 요청")
public class BatchPresignedUrlRequestDto {
    
    @Schema(
        description = "사용자 ID",
        example = "123",
        required = true
    )
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Integer userId;
    
    @Schema(
        description = "업로드할 파일명 목록",
        example = "[\"image1.jpg\", \"image2.jpg\", \"image3.jpg\"]",
        required = true
    )
    @NotEmpty(message = "파일명 목록은 필수입니다.")
    @Size(max = 20, message = "한 번에 최대 20개 파일까지 가능합니다.")
    private List<@Size(min = 1, max = 255, message = "파일명은 1-255자 사이여야 합니다.") String> fileNames;
    
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