package com.yourmode.yourmodebackend.domain.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "설문 질문 + 옵션 응답 DTO")
public class SurveyQuestionWithOptionsResponseDto {
    @Schema(description = "질문 ID", example = "1")
    private Integer id;
    @Schema(description = "질문 내용", example = "당신의 체형을 선택하세요.")
    private String content;
    @Schema(description = "질문 순서", example = "1")
    private Integer orderNumber;
    @Schema(description = "옵션 목록")
    private List<SurveyOptionResponseDto> options;
} 