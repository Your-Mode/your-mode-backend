package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyAnswerResponseDto;
import com.yourmode.yourmodebackend.domain.survey.dto.SurveyHistoryWithAnswersResponseDto;
import com.yourmode.yourmodebackend.domain.survey.entity.SurveyHistory;
import com.yourmode.yourmodebackend.domain.survey.repository.SurveyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyHistoryServiceImpl implements SurveyHistoryService {
    private final SurveyHistoryRepository surveyHistoryRepository;

    @Override
    public List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Long userId) {
        List<SurveyHistory> histories = surveyHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return histories.stream().map(h ->
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
    }
} 