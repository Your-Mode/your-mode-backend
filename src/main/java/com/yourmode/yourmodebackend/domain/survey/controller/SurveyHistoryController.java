package com.yourmode.yourmodebackend.domain.survey.controller;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyAnswerResponseDto;
import com.yourmode.yourmodebackend.domain.survey.dto.SurveyHistoryWithAnswersResponseDto;
import com.yourmode.yourmodebackend.domain.survey.entity.SurveyHistory;
import com.yourmode.yourmodebackend.domain.survey.repository.SurveyHistoryRepository;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/surveys/histories")
@RequiredArgsConstructor
@Tag(name = "Survey: 설문 내역/답변 API", description = "설문 내역과 답변을 조회하는 API")
public class SurveyHistoryController {
    private final SurveyHistoryRepository surveyHistoryRepository;

    @Operation(summary = "설문 내역+답변 전체 조회", description = "특정 사용자의 모든 설문 내역과 각 내역의 답변(질문/옵션/키값 포함)을 반환합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<BaseResponse<List<SurveyHistoryWithAnswersResponseDto>>> getSurveyHistoriesWithAnswers(@PathVariable Long userId) {
        List<SurveyHistory> histories = surveyHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<SurveyHistoryWithAnswersResponseDto> result = histories.stream().map(h ->
            SurveyHistoryWithAnswersResponseDto.builder()
                .historyId(h.getId().longValue())
                .createdAt(h.getCreatedAt())
                .answers(h.getAnswers().stream().map(a ->
                    SurveyAnswerResponseDto.builder()
                        .questionId(a.getSurveyQuestion().getId())
                        .questionContent(a.getSurveyQuestion().getContent())
                        .optionId(a.getSurveyOption().getId())
                        .optionContent(a.getSurveyOption().getContent())
                        .bodyTypeId(a.getSurveyOption().getBodyType().getId())
                        .build()
                ).collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
        return ResponseEntity.ok(BaseResponse.onSuccess(result));
    }
} 