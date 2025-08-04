package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyAnswersSubmitRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.request.SurveyTextAnswersRequestDto;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import java.util.List;

public interface SurveyService {
    List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions();
    List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Integer userId);
    List<SurveyAnswerResponseDto> getSurveyAnswersByHistory(Integer historyId, Integer userId);
    SurveyResultFastApiResponseDto analyzeSurveyAnswersWithFast(SurveyTextAnswersRequestDto dto, Integer userId);
    SurveyResultFastApiResponseDto analyzeSurveyHistoryWithFast(Integer historyId, Integer userId);
    SurveySaveResponseDto saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto, Integer userId);
    
    // 설문 결과 저장 및 조회 메서드들
    SurveyResultResponseDto saveSurveyResult(SurveyResultFastApiResponseDto result, Integer historyId);
    List<SurveyResultSummaryDto> getSurveyResultsByUserId(Integer userId);
    SurveyResultResponseDto getSurveyResultDetail(Integer resultId, Integer userId);

} 