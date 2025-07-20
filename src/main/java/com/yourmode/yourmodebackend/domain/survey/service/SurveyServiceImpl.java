package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.*;
import com.yourmode.yourmodebackend.domain.survey.entity.*;
import com.yourmode.yourmodebackend.domain.survey.repository.*;
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
    private static final String FASI_API_URL = "FASI_API_URL"; // 실제 주소로 교체

    @Override
    public BaseResponse<SurveyResultFasiApiResponseDto> analyzeSurveyAnswersWithFasi(SurveyTextAnswersRequestDto dto) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> fasiRequest = new HashMap<>();
        fasiRequest.put("answers", dto.getAnswers());
        fasiRequest.put("gender", dto.getGender());
        fasiRequest.put("height", dto.getHeight());
        fasiRequest.put("weight", dto.getWeight());
        SurveyResultFasiApiResponseDto result = restTemplate.postForObject(
            FASI_API_URL,
            fasiRequest,
            SurveyResultFasiApiResponseDto.class
        );
        return BaseResponse.onSuccess(result);
    }

    @Override
    public BaseResponse<List<SurveyQuestionWithOptionsResponseDto>> getAllQuestionsWithOptions() {
        // 기존 SurveyQuestionServiceImpl의 getAllQuestionsWithOptions() 로직을 여기에 구현
        // 예시: (실제 로직은 기존 서비스 참고)
        // return BaseResponse.onSuccess(...);
        throw new UnsupportedOperationException("구현 필요");
    }

    @Override
    public BaseResponse<List<SurveyHistoryWithAnswersResponseDto>> getSurveyHistoriesWithAnswers(Integer userId) {
        List<SurveyHistory> histories = surveyHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        List<SurveyHistoryWithAnswersResponseDto> result = histories.stream().<SurveyHistoryWithAnswersResponseDto>map(h ->
            SurveyHistoryWithAnswersResponseDto.builder()
                .historyId(h.getId())
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
        if (result.isEmpty()) {
            return new BaseResponse<>("COMMON200", "조회 결과가 없습니다.", result);
        }
        return BaseResponse.onSuccess(result);
    }

    @Override
    public BaseResponse<String> saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto) {
        SurveyHistory history;
        if (dto.getHistoryId() != null) {
            history = surveyHistoryRepository.findById(dto.getHistoryId())
                .orElseThrow(() -> new IllegalArgumentException("설문 이력이 존재하지 않습니다."));
        } else {
            throw new IllegalArgumentException("historyId는 필수입니다.");
        }
        for (SurveyAnswersSubmitRequestDto.Answer answerDto : dto.getAnswers()) {
            SurveyQuestion question = surveyQuestionRepository.findById(answerDto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("질문이 존재하지 않습니다."));
            SurveyOption option = surveyOptionRepository.findById(answerDto.getOptionId())
                .orElseThrow(() -> new IllegalArgumentException("옵션이 존재하지 않습니다."));
            SurveyAnswer answer = SurveyAnswer.builder()
                .surveyHistory(history)
                .surveyQuestion(question)
                .surveyOption(option)
                .build();
            surveyAnswerRepository.save(answer);
        }
        return BaseResponse.onSuccess("설문 답변이 저장되었습니다.");
    }

    @Override
    public BaseResponse<List<SurveyAnswerResponseDto>> getSurveyAnswersByHistory(Integer historyId) {
        List<SurveyAnswer> answers = surveyAnswerRepository.findAll().stream()
                .filter(a -> a.getSurveyHistory().getId().equals(historyId))
                .collect(Collectors.toList());
        List<SurveyAnswerResponseDto> result = answers.stream().map(a ->
                SurveyAnswerResponseDto.builder()
                        .questionId(a.getSurveyQuestion().getId())
                        .questionContent(a.getSurveyQuestion().getContent())
                        .optionId(a.getSurveyOption().getId())
                        .optionContent(a.getSurveyOption().getContent())
                        .bodyTypeId(a.getSurveyOption().getBodyType().getId())
                        .build()
        ).collect(Collectors.toList());
        return BaseResponse.onSuccess(result);
    }
} 