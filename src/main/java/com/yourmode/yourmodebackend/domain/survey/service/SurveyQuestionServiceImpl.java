package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.SurveyOptionResponseDto;
import com.yourmode.yourmodebackend.domain.survey.dto.SurveyQuestionWithOptionsResponseDto;
import com.yourmode.yourmodebackend.domain.survey.entity.SurveyQuestion;
import com.yourmode.yourmodebackend.domain.survey.repository.SurveyQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyQuestionServiceImpl implements SurveyQuestionService {
    private final SurveyQuestionRepository surveyQuestionRepository;

    @Override
    public List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions() {
        List<SurveyQuestion> questions = surveyQuestionRepository.findAllByOrderByOrderNumberAsc();
        return questions.stream().map(q ->
            SurveyQuestionWithOptionsResponseDto.builder()
                .id(q.getId())
                .content(q.getContent())
                .orderNumber(q.getOrderNumber())
                .options(q.getOptions().stream().map(opt ->
                    SurveyOptionResponseDto.builder()
                        .id(opt.getId())
                        .content(opt.getContent())
                        .bodyTypeId(opt.getBodyType().getId())
                        .build()
                ).collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
    }
} 