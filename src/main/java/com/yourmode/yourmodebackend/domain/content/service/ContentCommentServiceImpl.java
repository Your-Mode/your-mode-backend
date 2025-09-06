package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.CommentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.request.CommentUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.CommentResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.entity.ContentComment;
import com.yourmode.yourmodebackend.domain.content.repository.ContentCommentRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import com.yourmode.yourmodebackend.domain.content.status.ContentErrorStatus;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentCommentServiceImpl implements ContentCommentService {

    private final ContentCommentRepository contentCommentRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(Integer contentId, Integer userId, CommentCreateRequestDto request) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 콘텐츠 존재 여부 확인
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        
        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
        
        // 댓글 생성
        ContentComment comment = ContentComment.builder()
                .commentText(request.getCommentText())
                .content(content)
                .user(user)
                .build();
        
        ContentComment savedComment = contentCommentRepository.save(comment);
        
        return CommentResponseDto.builder()
                .id(savedComment.getId())
                .commentText(savedComment.getCommentText())
                .createdAt(savedComment.getCreatedAt())
                .userId(savedComment.getUser().getId())
                .userName(savedComment.getUser().getName())
                .contentId(savedComment.getContent().getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommentListResponseDto getComments(Integer contentId, Pageable pageable) {
        // 콘텐츠 존재 여부 확인
        if (!contentRepository.existsById(contentId)) {
            throw new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND);
        }
        
        // 댓글 목록 조회
        Page<ContentComment> commentPage = contentCommentRepository
                .findByContentIdWithUserOrderByCreatedAtDesc(contentId, pageable);
        
        // 댓글 총 개수 조회
        Long totalCount = contentCommentRepository.countByContentId(contentId);
        
        // DTO 변환
        List<CommentResponseDto> comments = commentPage.getContent().stream()
                .map(comment -> CommentResponseDto.builder()
                        .id(comment.getId())
                        .commentText(comment.getCommentText())
                        .createdAt(comment.getCreatedAt())
                        .userId(comment.getUser().getId())
                        .userName(comment.getUser().getName())
                        .contentId(comment.getContent().getId())
                        .build())
                .collect(Collectors.toList());
        
        return CommentListResponseDto.builder()
                .comments(comments)
                .totalCount(totalCount)
                .currentPage(commentPage.getNumber())
                .totalPages(commentPage.getTotalPages())
                .hasNext(commentPage.hasNext())
                .build();
    }

    @Override
    @Transactional
    public CommentResponseDto updateComment(Integer commentId, Integer userId, CommentUpdateRequestDto request) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 댓글 존재 여부 및 작성자 확인
        ContentComment comment = contentCommentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.COMMENT_NOT_FOUND));
        
        // 작성자 권한 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new RestApiException(ContentErrorStatus.COMMENT_ACCESS_DENIED);
        }
        
        // 댓글 수정
        comment.setCommentText(request.getCommentText());
        ContentComment updatedComment = contentCommentRepository.save(comment);
        
        return CommentResponseDto.builder()
                .id(updatedComment.getId())
                .commentText(updatedComment.getCommentText())
                .createdAt(updatedComment.getCreatedAt())
                .userId(updatedComment.getUser().getId())
                .userName(updatedComment.getUser().getName())
                .contentId(updatedComment.getContent().getId())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(Integer commentId, Integer userId) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 댓글 존재 여부 및 작성자 확인
        ContentComment comment = contentCommentRepository.findByIdWithUser(commentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.COMMENT_NOT_FOUND));
        
        // 작성자 권한 확인
        if (!comment.getUser().getId().equals(userId)) {
            throw new RestApiException(ContentErrorStatus.COMMENT_ACCESS_DENIED);
        }
        
        // 댓글 삭제
        contentCommentRepository.delete(comment);
    }
}
