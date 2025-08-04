package com.yourmode.yourmodebackend.domain.survey.service;

import com.yourmode.yourmodebackend.domain.survey.dto.request.*;
import com.yourmode.yourmodebackend.domain.survey.dto.response.*;
import com.yourmode.yourmodebackend.domain.survey.entity.*;
import com.yourmode.yourmodebackend.domain.survey.repository.*;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserProfile;
import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.repository.BodyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    private final SurveyResultRepository surveyResultRepository;
    private final UserRepository userRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private static final String FAST_API_URL = "https://fast.yourmode.co.kr/assistant/diagnosis"; // 실제 주소로 교체

    /**
     * 텍스트 답변과 신체정보를 받아 FastAPI 서버로 직접 전송하여 분석 결과를 반환하고 DB에 저장합니다.
     * 1) 요청 DTO 유효성 검사 (null 체크, answers 리스트 비어있음 체크)
     * 2) HTTP 헤더 설정 (Content-Type: application/json)
     * 3) RestTemplate을 사용하여 FastAPI 서버로 POST 요청 전송
     * 4) 응답 데이터를 SurveyResultFastApiResponseDto로 매핑
     * 5) 분석 결과를 DB에 저장
     * 6) 분석 결과 반환
     *
     * @param dto FastAPI 분석용 텍스트 답변+신체정보 요청 DTO
     * @param userId 사용자 ID (결과 저장용)
     * @return FastAPI 분석 결과 응답 DTO
     * @throws RestApiException
     *         - dto가 null인 경우 INVALID_FAST_REQUEST 상태로 예외 발생
     *         - answers 리스트가 null이거나 비어있는 경우 EMPTY_ANSWERS_LIST 상태로 예외 발생
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - FastAPI 서버 응답이 null인 경우 FAST_API_INVALID_RESPONSE 상태로 예외 발생
     *         - FastAPI 호출 실패 시 FAST_API_FAILED 상태로 예외 발생
     */
    @Override
    @Transactional
    public SurveyResultFastApiResponseDto analyzeSurveyAnswersWithFast(SurveyTextAnswersRequestDto dto, Integer userId) {
        if (dto == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_FAST_REQUEST);
        }
        
        if (dto.getAnswers() == null || dto.getAnswers().isEmpty()) {
            throw new RestApiException(SurveyErrorStatus.EMPTY_ANSWERS_LIST);
        }
        
        if (userId == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
        }
        
        // RestTemplate 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(0);     // 연결 타임아웃: 제한 없음
        factory.setReadTimeout(0);        // 읽기 타임아웃: 제한 없음
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<SurveyTextAnswersRequestDto> requestEntity = new HttpEntity<>(dto, headers);
        
        try {
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

            // 응답 데이터 검증 및 빌더 패턴으로 재구성
            SurveyResultFastApiResponseDto result = SurveyResultFastApiResponseDto.builder()
                    .bodyType(response.getBodyType() != null ? response.getBodyType() : "미분류")
                    .typeDescription(response.getTypeDescription() != null ? response.getTypeDescription() : "체형 분석 결과가 없습니다.")
                    .detailedFeatures(response.getDetailedFeatures() != null ? response.getDetailedFeatures() : "상세 특징 정보가 없습니다.")
                    .attractionPoints(response.getAttractionPoints() != null ? response.getAttractionPoints() : "매력 포인트 정보가 없습니다.")
                    .recommendedStyles(response.getRecommendedStyles() != null ? response.getRecommendedStyles() : "추천 스타일 정보가 없습니다.")
                    .avoidStyles(response.getAvoidStyles() != null ? response.getAvoidStyles() : "피해야 할 스타일 정보가 없습니다.")
                    .stylingFixes(response.getStylingFixes() != null ? response.getStylingFixes() : "스타일링 보완점 정보가 없습니다.")
                    .stylingTips(response.getStylingTips() != null ? response.getStylingTips() : "스타일링 팁 정보가 없습니다.")
                    .build();
            
            // 결과를 DB에 저장
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RestApiException(SurveyErrorStatus.INVALID_USER_ID));
                
                SurveyHistory history = SurveyHistory.builder()
                        .user(user)
                        .build();
                SurveyHistory savedHistory = surveyHistoryRepository.save(history);
                saveSurveyResult(result, savedHistory.getId());
            } catch (Exception e) {
                System.err.println("결과 저장 실패: " + e.getMessage());
                // 저장 실패해도 결과는 반환
            }
            
            return result;
                    
        } catch (Exception e) {
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
     * 7) 분석 결과를 DB에 저장
     * 8) 분석 결과 반환
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
    @Transactional
    public SurveyResultFastApiResponseDto analyzeSurveyHistoryWithFast(Integer historyId, Integer userId) {
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
            throw new RestApiException(SurveyErrorStatus.FORBIDDEN_SURVEY_ACCESS);
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
        
        // 7. FastAPI 호출 (이미 저장 기능 포함)
        SurveyResultFastApiResponseDto result = analyzeSurveyAnswersWithFast(fastRequest, userId);

        return result;
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
    public List<SurveyAnswerResponseDto> getSurveyAnswersByHistory(Integer historyId, Integer userId) {
        try {
            if (historyId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_HISTORY_ID);
            }
            
            if (userId == null) {
                throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
            }
            
            // 설문 이력 조회 및 사용자 본인 확인
            SurveyHistory history = surveyHistoryRepository.findById(historyId)
                    .orElseThrow(() -> new RestApiException(SurveyErrorStatus.SURVEY_NOT_FOUND));
            
            if (!history.getUser().getId().equals(userId)) {
                throw new RestApiException(SurveyErrorStatus.FORBIDDEN_SURVEY_ACCESS);
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
    
    /**
     * FastAPI 분석 결과를 DB에 저장합니다.
     * 1) historyId 유효성 검사
     * 2) 설문 이력 조회
     * 3) SurveyResult 엔티티 생성 및 저장
     * 4) 저장된 결과 반환
     *
     * @param result FastAPI 분석 결과
     * @param historyId 설문 이력 ID
     * @return 저장된 설문 결과 응답 DTO
     * @throws RestApiException
     *         - historyId가 null인 경우 INVALID_HISTORY_ID 상태로 예외 발생
     *         - 설문 이력을 찾을 수 없는 경우 SURVEY_NOT_FOUND 상태로 예외 발생
     *         - DB 저장 중 오류 발생 시 DB_RESULT_INSERT_FAILED 상태로 예외 발생
     */
    @Override
    @Transactional
    public SurveyResultResponseDto saveSurveyResult(SurveyResultFastApiResponseDto result, Integer historyId) {
        if (historyId == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_HISTORY_ID);
        }
        
        SurveyHistory history = surveyHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RestApiException(SurveyErrorStatus.SURVEY_NOT_FOUND));
        
        try {
            // 디버깅을 위한 로깅 추가
            System.out.println("=== saveSurveyResult Debug ===");
            System.out.println("historyId: " + historyId);
            System.out.println("result.getBodyType(): " + result.getBodyType());
            System.out.println("result.getTypeDescription(): " + result.getTypeDescription());
            System.out.println("result.getDetailedFeatures(): " + result.getDetailedFeatures());
            System.out.println("result.getAttractionPoints(): " + result.getAttractionPoints());
            System.out.println("result.getRecommendedStyles(): " + result.getRecommendedStyles());
            System.out.println("result.getAvoidStyles(): " + result.getAvoidStyles());
            System.out.println("result.getStylingFixes(): " + result.getStylingFixes());
            System.out.println("result.getStylingTips(): " + result.getStylingTips());
            
            // body_type_id를 찾기 위해 BodyType 조회
            BodyType bodyType = null;
            if (result.getBodyType() != null) {
                try {
                    // body_type_name으로 BodyType을 찾아보기
                    bodyType = bodyTypeRepository.findByName(result.getBodyType())
                            .orElse(null);
                } catch (Exception e) {
                    System.err.println("BodyType 조회 실패: " + e.getMessage());
                }
            }
            
            SurveyResult surveyResult = SurveyResult.builder()
                    .bodyTypeName(result.getBodyType())
                    .typeDescription(result.getTypeDescription())
                    .detailedFeatures(result.getDetailedFeatures())
                    .attractionPoints(result.getAttractionPoints())
                    .recommendedStyles(result.getRecommendedStyles())
                    .avoidStyles(result.getAvoidStyles())
                    .stylingFixes(result.getStylingFixes())
                    .stylingTips(result.getStylingTips())
                    .surveyHistory(history)
                    .bodyType(bodyType)  // bodyType 설정 추가
                    .build();
            
            System.out.println("SurveyResult 객체 생성 완료");
            
            SurveyResult savedResult = surveyResultRepository.save(surveyResult);
            
            System.out.println("DB 저장 완료, savedResult.getId(): " + savedResult.getId());
            
            return SurveyResultResponseDto.builder()
                    .resultId(savedResult.getId())
                    .bodyTypeName(savedResult.getBodyTypeName())
                    .typeDescription(savedResult.getTypeDescription())
                    .detailedFeatures(savedResult.getDetailedFeatures())
                    .attractionPoints(savedResult.getAttractionPoints())
                    .recommendedStyles(savedResult.getRecommendedStyles())
                    .avoidStyles(savedResult.getAvoidStyles())
                    .stylingFixes(savedResult.getStylingFixes())
                    .stylingTips(savedResult.getStylingTips())
                    .historyId(savedResult.getSurveyHistory().getId())
                    .createdAt(savedResult.getCreatedAt())
                    .build();
        } catch (Exception e) {
            System.err.println("=== saveSurveyResult Error ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RestApiException(SurveyErrorStatus.DB_RESULT_INSERT_FAILED);
        }
    }
    
    /**
     * 사용자의 모든 설문 결과 목록을 조회합니다.
     * 1) userId 유효성 검사
     * 2) 사용자의 설문 결과 목록 조회
     * 3) SurveyResultSummaryDto로 변환하여 반환
     *
     * @param userId 사용자 ID
     * @return 설문 결과 요약 목록
     * @throws RestApiException
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 설문 결과를 찾을 수 없는 경우 RESULT_NOT_FOUND 상태로 예외 발생
     *         - DB 조회 중 오류 발생 시 DB_RESULT_QUERY_FAILED 상태로 예외 발생
     */
    @Override
    public List<SurveyResultSummaryDto> getSurveyResultsByUserId(Integer userId) {
        if (userId == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
        }
        
        try {
            List<SurveyResult> results = surveyResultRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
            
            if (results.isEmpty()) {
                throw new RestApiException(SurveyErrorStatus.RESULT_NOT_FOUND);
            }
            
            return results.stream()
                    .map(result -> SurveyResultSummaryDto.builder()
                            .resultId(result.getId())
                            .bodyTypeName(result.getBodyTypeName())
                            .historyId(result.getSurveyHistory().getId())
                            .createdAt(result.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.DB_RESULT_QUERY_FAILED);
        }
    }
    
    /**
     * 특정 설문 결과의 상세 내용을 조회합니다.
     * 1) resultId와 userId 유효성 검사
     * 2) 설문 결과 조회 및 사용자 본인 확인
     * 3) 상세 내용 반환
     *
     * @param resultId 결과 ID
     * @param userId 사용자 ID
     * @return 설문 결과 상세 응답 DTO
     * @throws RestApiException
     *         - resultId가 null인 경우 INVALID_RESULT_ID 상태로 예외 발생
     *         - userId가 null인 경우 INVALID_USER_ID 상태로 예외 발생
     *         - 설문 결과를 찾을 수 없는 경우 RESULT_NOT_FOUND 상태로 예외 발생
     *         - 사용자 본인의 결과가 아닌 경우 RESULT_NOT_FOUND 상태로 예외 발생
     *         - DB 조회 중 오류 발생 시 DB_RESULT_QUERY_FAILED 상태로 예외 발생
     */
    @Override
    public SurveyResultResponseDto getSurveyResultDetail(Integer resultId, Integer userId) {
        if (resultId == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_RESULT_ID);
        }
        
        if (userId == null) {
            throw new RestApiException(SurveyErrorStatus.INVALID_USER_ID);
        }
        
        try {
            SurveyResult result = surveyResultRepository.findById(resultId)
                    .orElseThrow(() -> new RestApiException(SurveyErrorStatus.RESULT_NOT_FOUND));
            
            // 사용자 본인의 결과인지 확인
            if (!result.getSurveyHistory().getUser().getId().equals(userId)) {
                throw new RestApiException(SurveyErrorStatus.FORBIDDEN_RESULT_ACCESS);
            }
            
            return SurveyResultResponseDto.builder()
                    .resultId(result.getId())
                    .bodyTypeName(result.getBodyTypeName())
                    .typeDescription(result.getTypeDescription())
                    .detailedFeatures(result.getDetailedFeatures())
                    .attractionPoints(result.getAttractionPoints())
                    .recommendedStyles(result.getRecommendedStyles())
                    .avoidStyles(result.getAvoidStyles())
                    .stylingFixes(result.getStylingFixes())
                    .stylingTips(result.getStylingTips())
                    .historyId(result.getSurveyHistory().getId())
                    .createdAt(result.getCreatedAt())
                    .build();
        } catch (RestApiException e) {
            throw e;
        } catch (Exception e) {
            throw new RestApiException(SurveyErrorStatus.DB_RESULT_QUERY_FAILED);
        }
    }
} 