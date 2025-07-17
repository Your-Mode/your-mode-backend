package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyQuestionWithOptionsResponseDto;
import java.util.List;

public interface SurveyQuestionService {
    List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions();
} 