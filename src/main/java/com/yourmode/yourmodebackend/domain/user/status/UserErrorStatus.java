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
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH409", "이미 사용 중인 이메일입니다."),

    // 로그인
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH401", "비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH404", "해당 이메일의 사용자를 찾을 수 없습니다."),

    // 토큰
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH401-2", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401-3", "유효하지 않은 토큰입니다."),

    // 프로필
    INVALID_BODY_TYPE(HttpStatus.BAD_REQUEST, "PROFILE400", "유효하지 않은 체형 ID입니다.");

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
