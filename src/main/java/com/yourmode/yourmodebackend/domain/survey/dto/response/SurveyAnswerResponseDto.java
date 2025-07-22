package com.yourmode.yourmodebackend.domain.survey.dto.response;

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
    @Schema(description = "질문 내용", example = "피부는 어떤 느낌인가요?")
    private String questionContent;
    @Schema(description = "옵션 ID", example = "2")
    private Integer optionId;
    @Schema(description = "옵션 내용", example = "피부가 탄탄하고 쫀쫀한 탄력감이 느껴진다.")
    private String optionContent;
} 