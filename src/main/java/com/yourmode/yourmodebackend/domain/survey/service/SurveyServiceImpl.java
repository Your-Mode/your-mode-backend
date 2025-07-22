package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.request.*;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import com.yourmode.yourmodebackend.domain.survey.entity.*;
import com.yourmode.yourmodebackend.domain.survey.repository.*;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {
    private final SurveyHistoryRepository surveyHistoryRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final UserRepository userRepository;
    private static final String FASI_API_URL = "FASI_API_URL"; // 실제 주소로 교체

    @Override
    public SurveyResultFasiApiResponseDto analyzeSurveyAnswersWithFasi(SurveyTextAnswersRequestDto dto) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> fasiRequest = new HashMap<>();
        fasiRequest.put("answers", dto.getAnswers());
        fasiRequest.put("gender", dto.getGender());
        fasiRequest.put("height", dto.getHeight());
        fasiRequest.put("weight", dto.getWeight());
        return restTemplate.postForObject(
            FASI_API_URL,
            fasiRequest,
            SurveyResultFasiApiResponseDto.class
        );
    }

    @Override
    public List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions() {
        List<SurveyQuestion> questions = surveyQuestionRepository.findAllByOrderByOrderNumberAsc();
        return questions.stream().map(q -> SurveyQuestionWithOptionsResponseDto.builder()
                .questionId(q.getId())
                .questionContent(q.getContent())
                .options(q.getOptions().stream().map(o -> SurveyOptionResponseDto.builder()
                        .optionId(o.getId())
                        .optionContent(o.getContent())
                        .build())
                        .collect(Collectors.toList()))
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Integer userId) {
        List<SurveyHistory> histories = surveyHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<SurveyHistoryWithAnswersResponseDto> result = histories.stream().map(h ->
            SurveyHistoryWithAnswersResponseDto.builder()
                .historyId(h.getId())
                .createdAt(h.getCreatedAt())
                .answers(h.getAnswers().stream().map(a ->
                    SurveyAnswerResponseDto.builder()
                        .questionId(a.getSurveyQuestion().getId())
                        .questionContent(a.getSurveyQuestion().getContent())
                        .optionId(a.getSurveyOption().getId())
                        .optionContent(a.getSurveyOption().getContent())
                        .build()
                ).collect(Collectors.toList()))
                .build()
        ).collect(Collectors.toList());
        return result;
    }

    @Override
    public String saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto, Integer userId) {
        System.out.println("dto: " + dto);
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new IllegalArgumentException("answers 리스트는 비어있을 수 없습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 userId: " + userId));
        SurveyHistory history = SurveyHistory.builder()
                .user(user)
                .build();
        SurveyHistory savedHistory = surveyHistoryRepository.save(history);
        List<SurveyAnswer> answers = dto.getAnswers().stream().map(a -> {
            SurveyQuestion question = surveyQuestionRepository.findById(a.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 questionId: " + a.getQuestionId()));
            SurveyOption option = surveyOptionRepository.findById(a.getOptionId())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 optionId: " + a.getOptionId()));
            return SurveyAnswer.builder()
                    .surveyHistory(savedHistory)
                    .surveyQuestion(question)
                    .surveyOption(option)
                    .build();
        }).collect(Collectors.toList());
        surveyAnswerRepository.saveAll(answers);
        return "설문 답변이 저장되었습니다.";
    }

    @Override
    public List<SurveyAnswerResponseDto> getSurveyAnswersByHistory(Integer historyId) {
        List<SurveyAnswer> answers = surveyAnswerRepository.findAll().stream()
                .filter(a -> a.getSurveyHistory().getId().equals(historyId))
                .collect(Collectors.toList());
        List<SurveyAnswerResponseDto> result = answers.stream().map(a ->
                SurveyAnswerResponseDto.builder()
                        .questionId(a.getSurveyQuestion().getId())
                        .questionContent(a.getSurveyQuestion().getContent())
                        .optionId(a.getSurveyOption().getId())
                        .optionContent(a.getSurveyOption().getContent())
                        .build()
        ).collect(Collectors.toList());
        return result;
    }
} 