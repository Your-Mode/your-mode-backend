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
@Schema(description = "파일 삭제 응답")
public class FileDeleteResponseDto {
    
    @Schema(
        description = "삭제된 파일의 S3 URL",
        example = "https://your-bucket.s3.amazonaws.com/contents/users/123/test-image.jpg"
    )
    private String deletedFileUrl;
    
    @Schema(
        description = "삭제 성공 여부",
        example = "true"
    )
    private Boolean success;
    
    @Schema(
        description = "삭제 메시지",
        example = "파일이 성공적으로 삭제되었습니다."
    )
    private String message;
} 