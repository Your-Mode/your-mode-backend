package com.yourmode.yourmodebackend.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerRequestDto {
    private Integer historyId;
    private Integer questionId;
    private Integer optionId;
} 