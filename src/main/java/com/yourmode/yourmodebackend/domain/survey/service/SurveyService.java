package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyAnswersSubmitRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyTextAnswersRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import java.util.List;

public interface SurveyService {
    List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions();
    List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Integer userId);
    List<SurveyAnswerResponseDto> getSurveyAnswersByHistory(Integer historyId);
    SurveyResultFastApiResponseDto analyzeSurveyAnswersWithFast(SurveyTextAnswersRequestDto dto);
    SurveyResultFastApiResponseDto analyzeSurveyHistoryWithFast(Integer historyId, Integer userId);
    SurveySaveResponseDto saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto, Integer userId);
} 