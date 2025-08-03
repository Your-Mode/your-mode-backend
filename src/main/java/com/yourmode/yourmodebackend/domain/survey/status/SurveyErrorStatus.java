package com.yourmode.yourmodebackend.domain.survey.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 설문(Survey) 도메인에서 발생할 수 있는 에러 상태를 정의합니다.
 * 각 에러는 HTTP 상태, 에러 코드, 메시지를 포함합니다.
 */
@Getter
@AllArgsConstructor
public enum SurveyErrorStatus implements BaseCodeInterface {
    
    // 400 Bad Request - 클라이언트 요청 오류
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "SURVEY-400-001", "유효하지 않은 사용자 ID입니다."),
    INVALID_QUESTION_ID(HttpStatus.BAD_REQUEST, "SURVEY-400-002", "유효하지 않은 질문 ID입니다."),
    INVALID_OPTION_ID(HttpStatus.BAD_REQUEST, "SURVEY-400-003", "유효하지 않은 옵션 ID입니다."),
    INVALID_HISTORY_ID(HttpStatus.BAD_REQUEST, "SURVEY-400-004", "유효하지 않은 설문 이력 ID입니다."),
    EMPTY_ANSWERS_LIST(HttpStatus.BAD_REQUEST, "SURVEY-400-005", "답변 목록이 비어있습니다."),
    INVALID_FAST_REQUEST(HttpStatus.BAD_REQUEST, "SURVEY-400-006", "FastAPI 요청 데이터가 유효하지 않습니다."),

    // 404 Not Found - 리소스 없음
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-001", "설문을 찾을 수 없습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-002", "설문 문항을 찾을 수 없습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-003", "설문 답변을 찾을 수 없습니다."),

    // 500 Internal Server Error - 서버 오류 (조회 관련)
    DB_QUESTION_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY-500-001", "설문 질문 조회 중 오류가 발생했습니다."),
    DB_ANSWER_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY-500-002", "설문 답변 조회 중 오류가 발생했습니다."),
    DB_HISTORY_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY-500-003", "설문 이력 조회 중 오류가 발생했습니다."),
    
    // 500 Internal Server Error - 서버 오류 (저장 관련)
    DB_ANSWER_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY-500-101", "설문 답변을 DB에 저장하는 중 오류가 발생했습니다."),

    // 502 Bad Gateway - 외부 서비스 오류
    FAST_API_FAILED(HttpStatus.BAD_GATEWAY, "SURVEY-502-001", "FAST API 호출에 실패했습니다."),
    FAST_API_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "SURVEY-502-002", "FAST API에서 유효하지 않은 응답을 받았습니다.");

    private final HttpStatus httpStatus;
    private final boolean isSuccess = false;
    private final String code;
    private final String message;

    @Override
    public BaseCodeDto getCode() {
        return BaseCodeDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(isSuccess)
                .code(code)
                .message(message)
                .build();
    }
} 