package com.yourmode.yourmodebackend.domain.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "설문 내역 + 답변 응답 DTO")
public class SurveyHistoryWithAnswersResponseDto {
    @Schema(description = "설문 내역 ID", example = "1")
    private Long historyId;
    @Schema(description = "설문 응답 일시", example = "2025-07-17T12:00:00.000")
    private LocalDateTime createdAt;
    @Schema(description = "답변 목록")
    private List<SurveyAnswerResponseDto> answers;
} 