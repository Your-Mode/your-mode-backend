package com.yourmode.yourmodebackend.domain.survey.dto;

import lombok.Data;

@Data
public class SurveyResultFasiApiResponseDto {
    private String bodyType;
    private String typeDescription;
    private String detailedFeatures;
    private String attractionPoints;
    private String recommendedStyles;
    private String avoidStyles;
    private String stylingFixes;
    private String stylingTips;
} 