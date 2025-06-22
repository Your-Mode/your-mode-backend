package com.yourmode.yourmodebackend.global.common.base;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@JsonPropertyOrder({"timestamp", "isSuccess", "code", "message", "data"}) // JSON 응답 시 순서를 정의
public class BaseResponse<T> {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private boolean isSuccess;
    private String code;          // 응답 상태 코드
    private String message;    // 응답 메시지
    private T data;            // 응답 데이터

    // 데이터 포함
    public static <T> BaseResponse<T> of(BaseCodeDto baseCodeDto, T data) {
        return new BaseResponse<>(
                baseCodeDto.isSuccess(),
                baseCodeDto.getCode(),
                baseCodeDto.getMessage(),
                data
        );
    }

    // 데이터 없음
    public static <T> BaseResponse<T> of(BaseCodeDto baseCodeDto) {
        return new BaseResponse<>(
                baseCodeDto.isSuccess(),
                baseCodeDto.getCode(),
                baseCodeDto.getMessage(),
                null
        );
    }

}
