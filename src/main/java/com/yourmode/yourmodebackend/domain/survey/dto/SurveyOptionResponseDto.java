package com.yourmode.yourmodebackend.domain.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "설문 옵션 응답 DTO")
public class SurveyOptionResponseDto {
    @Schema(description = "옵션 ID", example = "1")
    private Integer id;
    @Schema(description = "옵션 내용", example = "스트레이트형")
    private String content;
    @Schema(description = "체형 타입 ID", example = "1")
    private Integer bodyTypeId;
} 