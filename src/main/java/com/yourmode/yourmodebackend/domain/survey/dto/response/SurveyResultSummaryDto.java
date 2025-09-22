package com.yourmode.yourmodebackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "설문 결과 요약 DTO")
public class SurveyResultSummaryDto {
    
    @Schema(description = "결과 ID", example = "1")
    private Integer resultId;
    
    @Schema(description = "체형 타입", example = "내추럴")
    private String bodyTypeName;
    
    @Schema(description = "설문 이력 ID", example = "1")
    private Integer historyId;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
} 