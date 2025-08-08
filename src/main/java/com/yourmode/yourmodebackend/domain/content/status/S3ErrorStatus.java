package com.yourmode.yourmodebackend.domain.content.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum S3ErrorStatus implements BaseCodeInterface {

    // 400 Bad Request - 클라이언트 요청 오류
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "S3-400-001", "유효하지 않은 파일명입니다."),
    INVALID_FILE_URL(HttpStatus.BAD_REQUEST, "S3-400-002", "유효하지 않은 파일 URL입니다."),
    INVALID_EXPIRATION_TIME(HttpStatus.BAD_REQUEST, "S3-400-003", "만료 시간은 1-60분 사이여야 합니다."),
    TOO_MANY_FILES(HttpStatus.BAD_REQUEST, "S3-400-004", "한 번에 최대 20개 파일까지 가능합니다."),
    EMPTY_FILE_LIST(HttpStatus.BAD_REQUEST, "S3-400-005", "파일명 목록이 비어있습니다."),
    INVALID_PRESIGNED_URL(HttpStatus.BAD_REQUEST, "S3-400-006", "유효하지 않은 Presigned URL입니다."),
    PRESIGNED_URL_EXPIRED(HttpStatus.BAD_REQUEST, "S3-400-007", "Presigned URL이 만료되었습니다."),
    UNAUTHORIZED_FILE_ACCESS(HttpStatus.BAD_REQUEST, "S3-400-008", "해당 파일에 대한 접근 권한이 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "S3-400-009", "유효하지 않은 사용자 ID입니다."),

    // 401 Unauthorized - 인증 실패
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "S3-401-001", "S3 작업을 위해 인증이 필요합니다."),

    // 403 Forbidden - 접근 권한 없음
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "S3-403-001", "S3 리소스에 대한 접근 권한이 없습니다."),

    // 404 Not Found - 리소스 없음
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "S3-404-001", "요청한 파일을 찾을 수 없습니다."),
    BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "S3-404-002", "S3 버킷을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "S3-404-003", "요청한 사용자를 찾을 수 없습니다."),

    // 409 Conflict - 리소스 충돌
    FILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "S3-409-001", "동일한 이름의 파일이 이미 존재합니다."),

    // 413 Payload Too Large - 파일 크기 초과
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "S3-413-001", "파일 크기가 허용된 최대 크기를 초과했습니다."),

    // 415 Unsupported Media Type - 지원하지 않는 파일 형식
    UNSUPPORTED_FILE_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "S3-415-001", "지원하지 않는 파일 형식입니다."),

    // 429 Too Many Requests - 요청 제한 초과
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "S3-429-001", "S3 요청 제한을 초과했습니다. 잠시 후 다시 시도해주세요."),

    // 500 Internal Server Error - 서버 오류
    S3_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-001", "S3 서버와의 연결에 실패했습니다."),
    PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-002", "Presigned URL 생성에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-003", "파일 업로드에 실패했습니다."),
    FILE_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-004", "파일 삭제에 실패했습니다."),
    FILE_EXISTENCE_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-005", "파일 존재 여부 확인에 실패했습니다."),
    BATCH_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3-500-006", "배치 작업 처리 중 오류가 발생했습니다."),

    // 502 Bad Gateway - 외부 서비스 오류
    S3_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "S3-502-001", "S3 서비스가 일시적으로 사용할 수 없습니다."),

    // 503 Service Unavailable - 서비스 불가
    S3_MAINTENANCE(HttpStatus.SERVICE_UNAVAILABLE, "S3-503-001", "S3 서비스가 점검 중입니다.");

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