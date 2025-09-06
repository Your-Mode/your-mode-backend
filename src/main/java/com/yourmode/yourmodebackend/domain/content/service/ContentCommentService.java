package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.CommentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.request.CommentUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentResponseDto;
import org.springframework.data.domain.Pageable;

public interface ContentCommentService {
    
    /**
     * 댓글을 작성합니다.
     * @param contentId 콘텐츠 ID
     * @param userId 사용자 ID
     * @param request 댓글 작성 요청 DTO
     * @return 작성된 댓글 정보
     */
    CommentResponseDto createComment(Integer contentId, Integer userId, CommentCreateRequestDto request);
    
    /**
     * 특정 콘텐츠의 댓글 목록을 조회합니다.
     * @param contentId 콘텐츠 ID
     * @param pageable 페이지네이션 정보
     * @return 댓글 목록과 페이지네이션 정보
     */
    CommentListResponseDto getComments(Integer contentId, Pageable pageable);
    
    /**
     * 댓글을 수정합니다.
     * @param commentId 댓글 ID
     * @param userId 사용자 ID
     * @param request 댓글 수정 요청 DTO
     * @return 수정된 댓글 정보
     */
    CommentResponseDto updateComment(Integer commentId, Integer userId, CommentUpdateRequestDto request);
    
    /**
     * 댓글을 삭제합니다.
     * @param commentId 댓글 ID
     * @param userId 사용자 ID
     */
    void deleteComment(Integer commentId, Integer userId);
}
