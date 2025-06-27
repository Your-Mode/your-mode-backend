package com.yourmode.yourmodebackend.global.common.exception;

import com.yourmode.yourmodebackend.global.common.base.BaseResponse;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.status.GlobalErrorStatus;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<BaseResponse<String>> handleRestApiException(RestApiException e) {
        BaseCodeDto errorCode = e.getErrorCode();
        return handleExceptionInternal(errorCode);
    }

    // DB 무결성 위반 (예: NOT NULL, UNIQUE 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("Data integrity violation", e);
        return handleExceptionInternal(GlobalErrorStatus._VALIDATION_ERROR.getCode());
    }

    // 잘못된 메서드 인자 타입 (쿼리 파라미터가 잘못된 경우)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseResponse<String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        return handleExceptionInternal(GlobalErrorStatus._METHOD_ARGUMENT_ERROR.getCode());
    }

    // @Valid 검증 실패 (RequestBody 등)
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String fieldName = fieldError.getField();
            String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(fieldName, errorMessage, (oldVal, newVal) -> oldVal + ", " + newVal);
        });

        return handleExceptionInternalArgs(GlobalErrorStatus._VALIDATION_ERROR.getCode(), errors);
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGenericException(Exception e) {
        log.error("Unhandled exception", e);
        return handleExceptionInternalFalse(GlobalErrorStatus._INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
    }

    // 공통 처리 로직들
    private ResponseEntity<BaseResponse<String>> handleExceptionInternal(BaseCodeDto errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(BaseCodeDto errorCode, Map<String, String> errorArgs) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorArgs));
    }

    private ResponseEntity<BaseResponse<String>> handleExceptionInternalFalse(BaseCodeDto errorCode, String errorPoint) {
        return ResponseEntity
                .status(errorCode.getHttpStatus().value())
                .body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorPoint));
    }
}
