package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyHistoryWithAnswersResponseDto;
import java.util.List;

public interface SurveyHistoryService {
    List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Integer userId);
} 