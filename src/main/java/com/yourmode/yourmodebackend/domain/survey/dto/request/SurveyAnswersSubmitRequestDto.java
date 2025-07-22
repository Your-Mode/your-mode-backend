package com.yourmode.yourmodebackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
// @Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모든 설문 답변 일괄 저장 요청 DTO")
@Getter
@Setter
public class SurveyAnswersSubmitRequestDto {
    @Schema(description = "답변 목록")
    @JsonProperty("answers")
    private List<Answer> answers;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "질문-옵션 답변 쌍")
    public static class Answer {
        @Schema(description = "질문 ID", example = "1")
        private Integer questionId;
        @Schema(description = "선택한 옵션 ID", example = "2")
        private Integer optionId;
    }

    public void setAnswers(List<Answer> answers) {
        System.out.println("setAnswers called: " + answers);
        this.answers = answers;
    }
} 