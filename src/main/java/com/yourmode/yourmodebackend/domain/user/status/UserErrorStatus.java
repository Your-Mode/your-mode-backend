package com.yourmode.yourmodebackend.domain.user.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseCodeInterface {

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
    // TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-401-003", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH-401-003", "유효하지 않은 토큰입니다.");

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
