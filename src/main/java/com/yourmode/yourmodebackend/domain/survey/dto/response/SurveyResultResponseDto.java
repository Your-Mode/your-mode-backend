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
@Schema(description = "설문 결과 응답 DTO")
public class SurveyResultResponseDto {
    
    @Schema(description = "결과 ID", example = "1")
    private Integer resultId;
    
    @Schema(description = "체형 타입", example = "내추럴")
    private String bodyTypeName;
    
    @Schema(description = "체형 설명", example = "당신의 체형은 내추럴 타입으로 진단됩니다...")
    private String typeDescription;
    
    @Schema(description = "상세 특징", example = "당신의 신체는 두께감 있고 육감적인 면이 강하게 드러납니다...")
    private String detailedFeatures;
    
    @Schema(description = "매력 포인트", example = "내추럴 체형의 가장 큰 매력 포인트는 바로 건강하고 탄력 있는 몸매입니다...")
    private String attractionPoints;
    
    @Schema(description = "추천 스타일", example = "내추럴 체형에는 레이어링과 구조적인 디자인이 잘 어울립니다...")
    private String recommendedStyles;
    
    @Schema(description = "피해야 할 스타일", example = "내추럴 체형은 지나치게 부드럽고 흐트러진 실루엣의 의상은 피하는 것이 좋습니다...")
    private String avoidStyles;
    
    @Schema(description = "스타일링 보완점", example = "내추럴 체형의 장점을 최대한 살리기 위해서는 레이어링을 활용하여 전체적인 균형을 잡는 것이 중요합니다...")
    private String stylingFixes;
    
    @Schema(description = "스타일링 팁", example = "내추럴 체형은 다양한 스타일을 소화할 수 있는 장점이 있습니다...")
    private String stylingTips;
    
    @Schema(description = "설문 이력 ID", example = "1")
    private Integer historyId;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
} 