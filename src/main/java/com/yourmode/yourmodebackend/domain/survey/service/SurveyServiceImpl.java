package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.request.*;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import com.yourmode.yourmodebackend.domain.survey.entity.*;
import com.yourmode.yourmodebackend.domain.survey.repository.*;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserProfile;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.domain.survey.status.SurveyErrorStatus;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyServiceImpl implements SurveyService {
    private final SurveyHistoryRepository surveyHistoryRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyOptionRepository surveyOptionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final UserRepository userRepository;
    private static final String FAST_API_URL = "FAST_API_URL"; // 실제 주소로 교체

    /**
     * 텍스트 답변과 신체정보를 받아 FastAPI 서버로 직접 전송하여 분석 결과를 반환합니다.
     * 1) 요청 DTO 유효성 검사 (null 체크, answers 리스트 비어있음 체크)
     * 2) HTTP 헤더 설정 (Content-Type: application/json)
     * 3) RestTemplate을 사용하여 FastAPI 서버로 POST 요청 전송
     * 4) 응답 데이터를 SurveyResultFastApiResponseDto로 매핑
     * 5) 분석 결과 반환
     *
     * @param dto FastAPI 분석용 텍스트 답변+신체정보 요청 DTO
     * @return FastAPI 분석 결과 응답 DTO
     * @throws RestApiException
     *         - dto가 null인 경우 INVALID_FAST_REQUEST 상태로 예외 발생
     *         - answers 리스트가 null이거나 비어있는 경우 EMPTY_ANSWERS_LIST 상태로 예외 발생
     *         - FastAPI 서버 응답이 null인 경우 FAST_API_INVALID_RESPONSE 상태로 예외 발생
     *         - FastAPI 호출 실패 시 FAST_API_FAILED 상태로 예외 발생
     */
    @Override
    public SurveyResultFastApiResponseDto analyzeSurveyAnswersWithFast(SurveyTextAnswersRequestDto dto) {
        try {
            if (dto == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_FAST_REQUEST);
            }
            
            if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.EMPTY_ANSWERS_LIST);
            }
            
            RestTemplate restTemplate = new RestTemplate();
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 디버깅을 위한 로그
            System.out.println("FastAPI 요청 URL: " + FAST_API_URL);
            System.out.println("FastAPI 요청 데이터: " + dto);
            
            HttpEntity<SurveyTextAnswersRequestDto> requestEntity = new HttpEntity<>(dto, headers);
            
            ResponseEntity<SurveyResultFastApiResponseDto> responseEntity = restTemplate.exchange(
                FAST_API_URL,
                HttpMethod.POST,
                requestEntity,
                SurveyResultFastApiResponseDto.class
            );
            
            SurveyResultFastApiResponseDto response = responseEntity.getBody();
            
            if (response == null) {
                throw new RestApiException(SurveyErrorStatus.FAST_API_INVALID_RESPONSE);
            }
            
            // 디버깅을 위한 로그
            System.out.println("FastAPI 응답: " + response);
            
            return response;
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            // 상세한 에러 로그
            System.err.println("FastAPI 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RestApiException(SurveyErrorStatus.FAST_API_FAILED);
        }
    }

    /**
     * 설문 이력 ID를 받아 해당 이력의 답변을 추출하여 FastAPI로 분석 요청을 보내고 결과를 반환합니다.
     * 1) historyId와 userId 유효성 검사 (null 체크)
     * 2) 설문 이력 조회 및 사용자 본인 확인
     * 3) 해당 이력의 답변 목록 조회
     * 4) 답변을 텍스트로 변환 (옵션 내용 추출)
     * 5) 사용자 프로필에서 신체 정보 가져오기
     * 6) SurveyTextAnswersRequestDto 생성 및 FastAPI 호출
     * 7) 분석 결과 반환
     *
     * @param historyId 분석할 설문 이력 ID
     * @param userId 현재 로그인한 사용자 ID
     * @return FastAPI 분석 결과 응답 DTO
     * @throws RestApiException
     *         - historyId가 null인 경우 INVALID_HISTORY_ID 상태로 예외 발생
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 설문 이력을 찾을 수 없는 경우 SURVEY_NOT_FOUND 상태로 예외 발생
     *         - 사용자 본인의 설문이 아닌 경우 SURVEY_NOT_FOUND 상태로 예외 발생
     *         - 답변을 찾을 수 없는 경우 ANSWER_NOT_FOUND 상태로 예외 발생
     *         - 사용자 프로필이 없는 경우 INVALID_USER_ID 상태로 예외 발생
     *         - FastAPI 호출 실패 시 FAST_API_FAILED 상태로 예외 발생
     */
    @Override
    public SurveyResultFastApiResponseDto analyzeSurveyHistoryWithFast(Integer historyId, Integer userId) {
        try {
            if (historyId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_HISTORY_ID);
            }
            
            if (userId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
            }
            
            // 1. 설문 이력 조회
            SurveyHistory history = surveyHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new RestApiException(SurveyErrorStatus.SURVEY_NOT_FOUND));
            
            // 2. 사용자 본인의 설문인지 확인
            if (!history.getUser().getId().equals(userId)) {
                throw new RestApiException(SurveyErrorStatus.SURVEY_NOT_FOUND);
            }
            
            // 3. 답변 목록 조회
            List<SurveyAnswer> answers = surveyAnswerRepository.findAll().stream()
                    .filter(a -> a != null && a.getSurveyHistory() != null && 
                               historyId.equals(a.getSurveyHistory().getId()))
                    .collect(Collectors.toList());
            
            if (answers.isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.ANSWER_NOT_FOUND);
            }
            
            // 4. 답변을 텍스트로 변환 (옵션 내용 추출)
            List<String> answerTexts = answers.stream()
                    .filter(a -> a.getSurveyOption() != null)
                    .map(a -> a.getSurveyOption().getContent())
                    .collect(Collectors.toList());
            
            // 5. 사용자 프로필에서 신체 정보 가져오기
            UserProfile userProfile = history.getUser().getProfile();
            if (userProfile == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
            }
            
            // 6. SurveyTextAnswersRequestDto 생성
            SurveyTextAnswersRequestDto fastRequest = new SurveyTextAnswersRequestDto();
            fastRequest.setAnswers(answerTexts);
            fastRequest.setGender(userProfile.getGender() != null ? userProfile.getGender().name() : "여성");
            fastRequest.setHeight(userProfile.getHeight() != null ? userProfile.getHeight().doubleValue() : 165.0);
            fastRequest.setWeight(userProfile.getWeight() != null ? userProfile.getWeight().doubleValue() : 55.0);
            
            // 7. FastAPI 호출
            return analyzeSurveyAnswersWithFast(fastRequest);
            
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.FAST_API_FAILED);
        }
    }

    /**
     * 모든 설문 질문과 각 질문의 옵션을 조회하여 반환합니다.
     * 1) 설문 질문 목록을 orderNumber 순으로 조회
     * 2) 각 질문의 옵션 목록 조회 및 null 필터링
     * 3) SurveyQuestionWithOptionsResponseDto로 변환
     * 4) 질문과 옵션 목록 반환
     *
     * @return 설문 질문과 옵션 목록이 포함된 응답 DTO 리스트
     * @throws RestApiException
     *         - 설문 질문을 찾을 수 없는 경우 QUESTION_NOT_FOUND 상태로 예외 발생
     *         - DB 조회 중 오류 발생 시 DB_QUESTION_QUERY_FAILED 상태로 예외 발생
     */
    @Override
    public List<SurveyQuestionWithOptionsResponseDto> getAllQuestionsWithOptions() {
        try {
            List<SurveyQuestion> questions = surveyQuestionRepository.findAllByOrderByOrderNumberAsc();
            
            if (questions == null || questions.isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.QUESTION_NOT_FOUND);
            }
            
            return questions.stream()
                .filter(q -> q != null) // null 질문 필터링
                .map(q -> {
                    // 질문의 옵션 목록이 null이거나 비어있는 경우 처리
                    List<SurveyOptionResponseDto> options = (q.getOptions() != null) 
                        ? q.getOptions().stream()
                            .filter(o -> o != null) // null 옵션 필터링
                            .map(o -> SurveyOptionResponseDto.builder()
                                .optionId(o.getId())
                                .optionContent(o.getContent())
                                .build())
                            .collect(Collectors.toList())
                        : new ArrayList<>();
                    
                    return SurveyQuestionWithOptionsResponseDto.builder()
                        .questionId(q.getId())
                        .questionContent(q.getContent())
                        .options(options)
                        .build();
                })
                .collect(Collectors.toList());
        } catch (RestApiException e) {
            // 이미 RestApiException인 경우 그대로 던지기
            throw e;
        } catch (Exception e) {
            // 기타 예외는 DB 조회 실패로 처리
            throw new RestApiException(SurveyErrorStatus.DB_QUESTION_QUERY_FAILED);
        }
    }

    /**
     * 로그인 유저가 모든 설문에 대해 선택한 답변을 한 번에 저장합니다.
     * 1) userId 유효성 검사 (null 체크)
     * 2) 사용자 존재 여부 확인
     * 3) SurveyHistory 생성 및 저장
     * 4) 각 답변에 대해 SurveyQuestion과 SurveyOption 조회
     * 5) SurveyAnswer 엔티티 생성 및 일괄 저장
     * 6) 저장 결과 반환 (historyId, answerCount)
     *
     * @param dto 설문 답변 일괄 저장 요청 DTO
     * @param userId 현재 로그인한 사용자 ID
     * @return 설문 저장 결과 응답 DTO (historyId, answerCount)
     * @throws RestApiException
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 사용자를 찾을 수 없는 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 질문 ID가 유효하지 않은 경우 INVALID_QUESTION_ID 상태로 예외 발생
     *         - 옵션 ID가 유효하지 않은 경우 INVALID_OPTION_ID 상태로 예외 발생
     *         - DB 저장 중 오류 발생 시 DB_ANSWER_INSERT_FAILED 상태로 예외 발생
     */
    @Override
    @Transactional
    public SurveySaveResponseDto saveSurveyAnswersBulk(SurveyAnswersSubmitRequestDto dto, Integer userId) {
        try {
            System.out.println("dto: " + dto);
            
            if (userId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
            }
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RestApiException(SurveyErrorStatus.INVALID_USER_ID));
            
            SurveyHistory history = SurveyHistory.builder()
                    .user(user)
                    .build();
            SurveyHistory savedHistory = surveyHistoryRepository.save(history);
            
            List<SurveyAnswer> answers = dto.getAnswers().stream().map(a -> {
                SurveyQuestion question = surveyQuestionRepository.findById(a.getQuestionId())
                        .orElseThrow(() -> new RestApiException(SurveyErrorStatus.INVALID_QUESTION_ID));
                SurveyOption option = surveyOptionRepository.findById(a.getOptionId())
                        .orElseThrow(() -> new RestApiException(SurveyErrorStatus.INVALID_OPTION_ID));
                return SurveyAnswer.builder()
                        .surveyHistory(savedHistory)
                        .surveyQuestion(question)
                        .surveyOption(option)
                        .build();
            }).collect(Collectors.toList());
            
            surveyAnswerRepository.saveAll(answers);
            
            return SurveySaveResponseDto.builder()
                    .historyId(savedHistory.getId())
                    .answerCount(answers.size())
                    .build();
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.DB_ANSWER_INSERT_FAILED);
        }
    }

    /**
     * 로그인 유저의 모든 설문 이력과 각 이력의 답변을 조회하여 반환합니다.
     * 1) userId 유효성 검사 (null 체크)
     * 2) 사용자의 설문 이력 목록을 생성일 역순으로 조회
     * 3) 각 이력의 답변 목록 조회 및 null 필터링
     * 4) SurveyHistoryWithAnswersResponseDto로 변환
     * 5) 이력과 답변 목록 반환
     *
     * @param userId 현재 로그인한 사용자 ID
     * @return 설문 이력과 답변 목록이 포함된 응답 DTO 리스트
     * @throws RestApiException
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 설문 이력을 찾을 수 없는 경우 SURVEY_NOT_FOUND 상태로 예외 발생
     *         - DB 조회 중 오류 발생 시 DB_HISTORY_QUERY_FAILED 상태로 예외 발생
     */
    @Override
    public List<SurveyHistoryWithAnswersResponseDto> getSurveyHistoriesWithAnswers(Integer userId) {
        try {
            if (userId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
            }
            
            List<SurveyHistory> histories = surveyHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            
            if (histories == null || histories.isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.SURVEY_NOT_FOUND);
            }
            
            return histories.stream()
                .filter(h -> h != null)
                .map(h -> {
                    List<SurveyAnswerResponseDto> answers = (h.getAnswers() != null) 
                        ? h.getAnswers().stream()
                            .filter(a -> a != null && a.getSurveyQuestion() != null && a.getSurveyOption() != null)
                            .map(a -> SurveyAnswerResponseDto.builder()
                                .questionId(a.getSurveyQuestion().getId())
                                .questionContent(a.getSurveyQuestion().getContent())
                                .optionId(a.getSurveyOption().getId())
                                .optionContent(a.getSurveyOption().getContent())
                                .build())
                            .collect(Collectors.toList())
                        : new ArrayList<>();
                    
                    return SurveyHistoryWithAnswersResponseDto.builder()
                        .historyId(h.getId())
                        .createdAt(h.getCreatedAt())
                        .answers(answers)
                        .build();
                })
                .collect(Collectors.toList());
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.DB_HISTORY_QUERY_FAILED);
        }
    }

    /**
     * 설문 이력 ID로 해당 이력의 모든 답변을 조회하여 반환합니다.
     * 1) historyId 유효성 검사 (null 체크)
     * 2) 해당 이력의 답변 목록 조회
     * 3) null 필터링 및 SurveyAnswerResponseDto로 변환
     * 4) 답변 목록 반환
     *
     * @param historyId 조회할 설문 이력 ID
     * @return 설문 답변 목록이 포함된 응답 DTO 리스트
     * @throws RestApiException
     *         - historyId가 null인 경우 INVALID_HISTORY_ID 상태로 예외 발생
     *         - 설문 답변을 찾을 수 없는 경우 ANSWER_NOT_FOUND 상태로 예외 발생
     *         - DB 조회 중 오류 발생 시 DB_ANSWER_QUERY_FAILED 상태로 예외 발생
     */
    @Override
    public List<SurveyAnswerResponseDto> getSurveyAnswersByHistory(Integer historyId) {
        try {
            if (historyId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_HISTORY_ID);
            }
            
            List<SurveyAnswer> answers = surveyAnswerRepository.findAll().stream()
                    .filter(a -> a != null && a.getSurveyHistory() != null && 
                               historyId.equals(a.getSurveyHistory().getId()))
                    .collect(Collectors.toList());
            
            if (answers.isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.ANSWER_NOT_FOUND);
            }
            
            return answers.stream()
                    .filter(a -> a.getSurveyQuestion() != null && a.getSurveyOption() != null)
                    .map(a -> SurveyAnswerResponseDto.builder()
                            .questionId(a.getSurveyQuestion().getId())
                            .questionContent(a.getSurveyQuestion().getContent())
                            .optionId(a.getSurveyOption().getId())
                            .optionContent(a.getSurveyOption().getContent())
                            .build())
                    .collect(Collectors.toList());
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.DB_ANSWER_QUERY_FAILED);
        }
    }
} 