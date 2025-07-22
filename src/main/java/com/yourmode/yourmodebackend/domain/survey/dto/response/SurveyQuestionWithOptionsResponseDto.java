package com.yourmode.yourmodebackend.domain.survey.dto.response;

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
    private Integer questionId;
    @Schema(description = "질문 내용", example = "피부는 어떤 느낌인가요?")
    private String questionContent;
    @Schema(description = "옵션 목록")
    private List<SurveyOptionResponseDto> options;
} 