package com.yourmode.yourmodebackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "FasiAPI 분석 결과 응답 DTO")
public class SurveyResultFasiApiResponseDto {
    @Schema(description = "체형 타입", example = "스트레이트형")
    private String bodyType;
    @Schema(description = "체형 설명", example = "탄탄하고 직선적인 느낌의 체형입니다.")
    private String typeDescription;
    @Schema(description = "상세 특징", example = "근육이 잘 붙고, 어깨가 넓은 편입니다.")
    private String detailedFeatures;
    @Schema(description = "매력 포인트", example = "탄탄한 상체")
    private String attractionPoints;
    @Schema(description = "추천 스타일", example = "슬림핏, 미니멀룩")
    private String recommendedStyles;
    @Schema(description = "피해야 할 스타일", example = "오버핏")
    private String avoidStyles;
    @Schema(description = "스타일링 보완점", example = "허리 라인을 강조")
    private String stylingFixes;
    @Schema(description = "스타일링 팁", example = "밝은 컬러 활용")
    private String stylingTips;
} 