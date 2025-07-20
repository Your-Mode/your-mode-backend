package com.yourmode.yourmodebackend.domain.survey.dto;

import lombok.Data;
import java.util.List;

@Data
public class SurveyTextAnswersRequestDto {
    private List<String> answers;
    private String gender;
    private Double height;
    private Double weight;
} 