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
    // 설문 관련 에러
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-001", "설문을 찾을 수 없습니다."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-002", "설문 문항을 찾을 수 없습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-003", "설문 선택지를 찾을 수 없습니다."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "SURVEY-404-004", "설문 답변을 찾을 수 없습니다."),
    INVALID_ANSWER(HttpStatus.BAD_REQUEST, "SURVEY-400-001", "유효하지 않은 설문 답변입니다."),
    DUPLICATE_ANSWER(HttpStatus.CONFLICT, "SURVEY-409-001", "이미 답변한 설문입니다."),
    DB_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SURVEY-500-001", "설문 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    FASI_API_FAILED(HttpStatus.BAD_GATEWAY, "SURVEY-502-001", "FASI API 호출에 실패했습니다.");

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