package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.*;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import java.util.List;

public interface SurveyService {
    // Fasi 분석
    BaseResponse<SurveyResultFasiApiResponseDto> analyzeSurveyAnswersWithFasi(SurveyTextAnswersRequestDto dto);

    // 설문 질문+옵션 전체 조회
    BaseResponse<List<SurveyQuestionWithOptionsResponseDto>> getAllQuestionsWithOptions();

    // 설문 내역+답변 전체 조회
    BaseResponse<List<SurveyHistoryWithAnswersResponseDto>> getSurveyHistoriesWithAnswers(Integer userId);

    // 설문 답변 일괄 저장
    BaseResponse<String> saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto);

    // 설문 답변 목록 조회 (historyId 기준)
    BaseResponse<List<SurveyAnswerResponseDto>> getSurveyAnswersByHistory(Integer historyId);
} 