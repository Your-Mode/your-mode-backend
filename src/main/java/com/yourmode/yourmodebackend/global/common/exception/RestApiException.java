package com.yourmode.yourmodebackend.global.common.exception;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;

@AllArgsConstructor // 생성자를 만들어주는 어노테이션
public class RestApiException extends RuntimeException {

    private final BaseCodeInterface errorCode; //추상화 시킨 인터페이스를 받아서 사용

    //추상화 시킨 ErrorCode의 getrCode()를 사용하여 ErrorCode를 반환
    public BaseCodeDto getErrorCode() {
        return this.errorCode.getCode();
    }
}