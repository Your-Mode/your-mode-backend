package com.yourmode.yourmodebackend.domain.survey.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswersSubmitRequestDto {
    private Integer historyId; // 설문 이력 ID (없으면 새로 생성)
    private List<Answer> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        private Integer questionId;
        private Integer optionId;
    }
} 