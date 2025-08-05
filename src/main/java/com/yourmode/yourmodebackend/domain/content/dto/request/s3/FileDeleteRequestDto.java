package com.yourmode.yourmodebackend.domain.content.dto.request.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "S3 파일 삭제 요청")
public class FileDeleteRequestDto {
    
    @Schema(
        description = "사용자 ID",
        example = "123",
        required = true
    )
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Integer userId;
    
    @Schema(
        description = "삭제할 파일의 S3 URL",
        example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg",
        required = true
    )
    @NotBlank(message = "파일 URL은 필수입니다.")
    @Pattern(
        regexp = "^https://.*\\.s3\\.amazonaws\\.com/.*$",
        message = "유효한 S3 URL 형식이 아닙니다."
    )
    private String fileUrl;
} 