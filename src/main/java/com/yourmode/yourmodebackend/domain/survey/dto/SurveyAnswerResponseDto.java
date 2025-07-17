package com.yourmode.yourmodebackend.domain.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "설문 답변 응답 DTO")
public class SurveyAnswerResponseDto {
    @Schema(description = "질문 ID", example = "1")
    private Integer questionId;
    @Schema(description = "질문 내용", example = "당신의 체형을 선택하세요.")
    private String questionContent;
    @Schema(description = "옵션 ID", example = "2")
    private Integer optionId;
    @Schema(description = "옵션 내용", example = "스트레이트형")
    private String optionContent;
    @Schema(description = "체형 타입 ID", example = "1")
    private Long bodyTypeId;
} 