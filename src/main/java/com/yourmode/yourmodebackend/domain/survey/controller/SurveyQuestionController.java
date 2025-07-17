package com.yourmode.yourmodebackend.domain.survey.controller;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyQuestionWithOptionsResponseDto;
import com.yourmode.yourmodebackend.domain.survey.service.SurveyQuestionService;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/surveys/questions")
@RequiredArgsConstructor
@Tag(name = "Survey: 설문 질문/옵션 API", description = "설문 질문과 옵션을 조회하는 API")
public class SurveyQuestionController {
    private final SurveyQuestionService surveyQuestionService;

    @Operation(summary = "설문 질문+옵션 전체 조회", description = "모든 설문 질문과 각 질문의 옵션(키값 포함)을 반환합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<List<SurveyQuestionWithOptionsResponseDto>>> getAllQuestionsWithOptions() {
        return ResponseEntity.ok(BaseResponse.onSuccess(surveyQuestionService.getAllQuestionsWithOptions()));
    }
} 