package com.yourmode.yourmodebackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@Data
@Schema(description = "FastAPI 분석용 텍스트 답변+신체정보 요청 DTO")
public class SurveyTextAnswersRequestDto {
    
    @Schema(description = "질문별 텍스트 답변 목록", example = "['답변1', '답변2', ...]", required = true)
    @NotEmpty(message = "답변 목록이 비어있습니다.")
    @Size(min = 15, max = 15, message = "답변은 15개여야 합니다.")
    private List<String> answers;
    
    @Schema(description = "성별", example = "여성", required = true)
    @NotNull(message = "성별은 필수입니다.")
    @Pattern(regexp = "^(남성|여성)$", message = "성별은 '남성' 또는 '여성'이어야 합니다.")
    private String gender;
    
    @Schema(description = "키 (cm)", example = "164.5", required = true)
    @NotNull(message = "키는 필수입니다.")
    @Min(value = 100, message = "키는 100cm 이상이어야 합니다.")
    @Max(value = 250, message = "키는 250cm 이하여야 합니다.")
    private Double height;
    
    @Schema(description = "몸무게 (kg)", example = "55.2", required = true)
    @NotNull(message = "몸무게는 필수입니다.")
    @Min(value = 30, message = "몸무게는 30kg 이상이어야 합니다.")
    @Max(value = 200, message = "몸무게는 200kg 이하여야 합니다.")
    private Double weight;
} 