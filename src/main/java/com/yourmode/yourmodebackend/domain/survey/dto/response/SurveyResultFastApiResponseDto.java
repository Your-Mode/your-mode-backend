package com.yourmode.yourmodebackend.domain.survey.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"bodyType", "typeDescription", "detailedFeatures", "attractionPoints", "recommendedStyles", "avoidStyles", "stylingFixes", "stylingTips"})
@Schema(description = "FastAPI 분석 결과 응답 DTO")
public class SurveyResultFastApiResponseDto {
    @JsonProperty("body_type")
    @Schema(description = "체형 타입", example = "내추럴")
    private String bodyType;
    
    @JsonProperty("type_description")
    @Schema(description = "체형 설명", example = "당신의 체형은 내추럴 타입으로 진단됩니다...")
    private String typeDescription;
    
    @JsonProperty("detailed_features")
    @Schema(description = "상세 특징", example = "당신의 신체는 두께감 있고 육감적인 면이 강하게 드러납니다...")
    private String detailedFeatures;
    
    @JsonProperty("attraction_points")
    @Schema(description = "매력 포인트", example = "내추럴 체형의 가장 큰 매력 포인트는 바로 건강하고 탄력 있는 몸매입니다...")
    private String attractionPoints;
    
    @JsonProperty("recommended_styles")
    @Schema(description = "추천 스타일", example = "내추럴 체형에는 레이어링과 구조적인 디자인이 잘 어울립니다...")
    private String recommendedStyles;
    
    @JsonProperty("avoid_styles")
    @Schema(description = "피해야 할 스타일", example = "내추럴 체형은 지나치게 부드럽고 흐트러진 실루엣의 의상은 피하는 것이 좋습니다...")
    private String avoidStyles;
    
    @JsonProperty("styling_fixes")
    @Schema(description = "스타일링 보완점", example = "내추럴 체형의 장점을 최대한 살리기 위해서는 레이어링을 활용하여 전체적인 균형을 잡는 것이 중요합니다...")
    private String stylingFixes;
    
    @JsonProperty("styling_tips")
    @Schema(description = "스타일링 팁", example = "내추럴 체형은 다양한 스타일을 소화할 수 있는 장점이 있습니다...")
    private String stylingTips;
} 