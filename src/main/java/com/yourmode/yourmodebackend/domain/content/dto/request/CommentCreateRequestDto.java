package com.yourmode.yourmodebackend.domain.content.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequestDto {
    @Schema(description = "댓글 내용", example = "정말 유용한 정보네요!")
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 1000, message = "댓글은 1000자를 초과할 수 없습니다.")
    private String commentText;
}
