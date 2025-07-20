package com.yourmode.yourmodebackend.domain.survey.controller;

import com.yourmode.yourmodebackend.domain.survey.dto.*;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.domain.survey.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
@Tag(name = "Survey: 설문 관련 API", description = "설문 질문/답변/이력 관련 API")
public class SurveyController {
    private final SurveyService surveyService;

    @Operation(summary = "설문 질문+옵션 전체 조회", description = "모든 설문 질문과 각 질문의 옵션(키값 포함)을 반환합니다.")
    @GetMapping("/questions")
    public ResponseEntity<BaseResponse<List<SurveyQuestionWithOptionsResponseDto>>> getAllQuestionsWithOptions() {
        return ResponseEntity.ok(surveyService.getAllQuestionsWithOptions());
    }

    @Operation(summary = "유저 설문 전체 답변 저장", description = "유저가 모든 설문에 대해 선택한 답변을 한 번에 저장합니다.")
    @PostMapping("/answers/bulk")
    public ResponseEntity<BaseResponse<String>> saveSurveyAnswersBulk(@RequestBody SurveyAnswersSubmitRequestDto dto) {
        return ResponseEntity.ok(surveyService.saveSurveyAnswersBulk(dto));
    }

    @Operation(summary = "설문 내역+답변 전체 조회", description = "특정 사용자의 모든 설문 내역과 각 내역의 답변(질문/옵션/키값 포함)을 반환합니다.")
    @GetMapping("/histories/user/{userId}")
    public ResponseEntity<BaseResponse<List<SurveyHistoryWithAnswersResponseDto>>> getSurveyHistoriesWithAnswers(@PathVariable Integer userId) {
        return ResponseEntity.ok(surveyService.getSurveyHistoriesWithAnswers(userId));
    }

    @Operation(summary = "유저 설문 답변 목록 조회", description = "설문 이력 ID로 해당 이력의 모든 답변을 조회합니다.")
    @GetMapping("/answers/history/{historyId}")
    public ResponseEntity<BaseResponse<List<SurveyAnswerResponseDto>>> getSurveyAnswersByHistory(@PathVariable Integer historyId) {
        return ResponseEntity.ok(surveyService.getSurveyAnswersByHistory(historyId));
    }

    @Operation(summary = "외부 FasiAPI로 설문 답변 결과 분석", description = "텍스트 답변과 신체정보를 받아 FasiAPI 서버로 전송, 분석 결과를 반환합니다.")
    @PostMapping("/answers/fasi")
    public ResponseEntity<BaseResponse<SurveyResultFasiApiResponseDto>> analyzeSurveyAnswersWithFasi(
            @RequestBody SurveyTextAnswersRequestDto dto) {
        return ResponseEntity.ok(surveyService.analyzeSurveyAnswersWithFasi(dto));
    }
} 