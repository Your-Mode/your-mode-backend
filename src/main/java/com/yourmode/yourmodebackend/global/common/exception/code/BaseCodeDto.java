package com.yourmode.yourmodebackend.global.common.exception.code;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseCodeDto {
    private HttpStatus httpStatus;
    private boolean isSuccess;
    private String code;
    private String message;
}
