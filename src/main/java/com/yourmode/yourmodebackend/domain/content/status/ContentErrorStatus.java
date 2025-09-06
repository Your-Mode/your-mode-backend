package com.yourmode.yourmodebackend.domain.content.status;

import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeDto;
import com.yourmode.yourmodebackend.global.common.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ContentErrorStatus implements BaseCodeInterface {
    CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-001", "해당 컨텐츠를 찾을 수 없습니다."),
    FORBIDDEN_CONTENT_ACCESS(HttpStatus.FORBIDDEN, "CONT-403-001", "해당 컨텐츠에 대한 접근 권한이 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-002", "해당 카테고리를 찾을 수 없습니다."),
    BODY_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-003", "해당 바디타입을 찾을 수 없습니다."),
    INVALID_CONTENT_PARAMETER(HttpStatus.BAD_REQUEST, "CONT-400-001", "컨텐츠 요청 파라미터가 유효하지 않습니다."),
    DB_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-001", "컨텐츠 정보를 DB에 저장하는 중 오류가 발생했습니다."),
    DB_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-002", "컨텐츠 정보 수정 중 오류가 발생했습니다."),
    DB_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-003", "컨텐츠 정보 삭제 중 오류가 발생했습니다."),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-004", "해당 컨텐츠 블록을 찾을 수 없습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-005", "해당 컨텐츠 이미지를 찾을 수 없습니다."),
    
    // 댓글 관련 에러
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-006", "해당 댓글을 찾을 수 없습니다."),
    COMMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CONT-403-002", "댓글에 대한 접근 권한이 없습니다."),
    COMMENT_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-004", "댓글 수정 중 오류가 발생했습니다."),
    COMMENT_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-005", "댓글 삭제 중 오류가 발생했습니다."),
    
    // 좋아요 관련 에러
    LIKE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONT-409-001", "이미 좋아요를 누른 콘텐츠입니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "CONT-404-007", "해당 좋아요를 찾을 수 없습니다."),
    LIKE_ADD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-006", "좋아요 추가 중 오류가 발생했습니다."),
    LIKE_REMOVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONT-500-007", "좋아요 제거 중 오류가 발생했습니다.");

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