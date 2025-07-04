package com.yourmode.yourmodebackend.domain.user.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {

    INVALID_BODY_TYPE(HttpStatus.BAD_REQUEST, "AUTH-400-003", "유효하지 않은 체형 타입입니다."),
    
    // 회원가입
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-409-001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "AUTH-409-002", "이미 사용 중인 전화번호입니다."),
    DB_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-001", "사용자 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    DB_CREDENTIAL_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-002", "사용자 인증정보 저장 중 오류가 발생했습니다."),
    DB_PROFILE_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-003", "사용자 프로필 저장 중 오류가 발생했습니다."),
    DB_TOKEN_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-004", "사용자 토큰 저장 중 오류가 발생했습니다."),

    // 카카오 관련
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO-502-001", "카카오 토큰 요청에 실패했습니다."),
    KAKAO_USERINFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO-502-002", "카카오 사용자 정보 요청에 실패했습니다."),
    KAKAO_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "KAKAO-503-001", "카카오 서버와의 통신이 불가능합니다."),
    KAKAO_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "KAKAO-400-001", "카카오 계정에서 이메일을 받아올 수 없습니다."),

    // 로그인
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-401-001", "인증에 실패했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH-401-002", "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-404-001", "해당 이메일의 사용자를 찾을 수 없습니다."),

    // 토큰
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-003", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-003", "유효하지 않은 토큰입니다."),

    // 로그아웃 관련 에러
    LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-005", "로그아웃 처리 중 오류가 발생했습니다."),
    LOGOUT_NO_ACTIVE_SESSION(HttpStatus.BAD_REQUEST, "AUTH-400-001", "로그아웃할 활성 세션이 존재하지 않습니다."),

    // PasswordResetService 관련 에러 추가
    USER_NOT_FOUND_PHONE_NUMBER(HttpStatus.NOT_FOUND, "AUTH-404-002", "해당 전화번호로 가입된 사용자가 존재하지 않습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-006", "SMS 인증 코드 전송 중 오류가 발생했습니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "AUTH-400-002", "인증 코드가 유효하지 않습니다."),
    UNAUTHORIZED_PASSWORD_CHANGE(HttpStatus.UNAUTHORIZED, "AUTH-401-004", "비밀번호 변경 권한이 없습니다. 인증을 먼저 진행해주세요."),
    SMS_SEND_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AUTH-429-001", "SMS 발송 요청 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요."),
    PASSWORD_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH-500-007", "비밀번호 변경에 실패했습니다."),
    CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-404-003", "사용자 자격증명 정보를 찾을 수 없습니다.");

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
