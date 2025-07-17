package com.yourmode.yourmodebackend.domain.request.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RequestErrorStatus implements BaseCodeInterface {
    // 요청 관련 에러
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ-404-001", "해당 콘텐츠 신청을 찾을 수 없습니다."),
    INVALID_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "REQ-400-001", "유효하지 않은 신청 상태입니다."),
    FORBIDDEN_REQUEST_ACCESS(HttpStatus.FORBIDDEN, "REQ-403-001", "해당 요청에 대한 접근 권한이 없습니다."),
    DUPLICATE_REQUEST(HttpStatus.CONFLICT, "REQ-409-001", "이미 신청된 콘텐츠입니다."),
    DB_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REQ-500-001", "콘텐츠 신청 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    DB_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REQ-500-002", "콘텐츠 신청 정보 수정 중 오류가 발생했습니다."),
    DB_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REQ-500-003", "콘텐츠 신청 정보 삭제 중 오류가 발생했습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ-404-002", "해당 카테고리를 찾을 수 없습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "REQ-400-002", "요청 파라미터가 유효하지 않습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "REQ-409-002", "이미 처리된 요청입니다."),
    STATUS_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "REQ-404-003", "신청 상태 변경 이력을 찾을 수 없습니다.");

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