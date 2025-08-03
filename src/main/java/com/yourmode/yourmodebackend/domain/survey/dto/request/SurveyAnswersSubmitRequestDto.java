package com.yourmode.yourmodebackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "모든 설문 답변 일괄 저장 요청 DTO")
public class SurveyAnswersSubmitRequestDto {
    
    @Schema(description = "답변 목록", required = true)
    @NotEmpty(message = "답변 목록이 비어있습니다.")
    @Size(min = 15, max = 15, message = "답변은 15개여야 합니다.")
    @Valid
    private List<Answer> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "질문-옵션 답변 쌍")
    public static class Answer {
        @Schema(description = "질문 ID", example = "1", required = true)
        @NotNull(message = "유효하지 않은 질문 ID입니다.")
        @Min(value = 1, message = "질문 ID는 1 이상이어야 합니다.")
        private Integer questionId;
        
        @Schema(description = "선택한 옵션 ID", example = "2", required = true)
        @NotNull(message = "유효하지 않은 옵션 ID입니다.")
        @Min(value = 1, message = "옵션 ID는 1 이상이어야 합니다.")
        private Integer optionId;
    }
} 