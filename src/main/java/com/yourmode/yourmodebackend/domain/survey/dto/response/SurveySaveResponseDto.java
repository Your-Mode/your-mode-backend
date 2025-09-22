package com.yourmode.yourmodebackend.domain.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"message", "historyId", "answerCount"})
@Schema(description = "설문 답변 저장 응답 DTO")
public class SurveySaveResponseDto {
    @Schema(description = "생성된 설문 이력 ID", example = "1")
    private Integer historyId;
    
    @Schema(description = "저장된 답변 개수", example = "15")
    private Integer answerCount;
} 