package com.yourmode.yourmodebackend.domain.survey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "FasiAPI 분석용 텍스트 답변+신체정보 요청 DTO")
public class SurveyTextAnswersRequestDto {
    @Schema(description = "질문별 텍스트 답변 목록", example = "['답변1', '답변2', ...]")
    private List<String> answers;
    @Schema(description = "성별", example = "여성")
    private String gender;
    @Schema(description = "키", example = "164.5")
    private Double height;
    @Schema(description = "몸무게", example = "55.2")
    private Double weight;
} 