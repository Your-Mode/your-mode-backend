package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.LikeResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.entity.ContentLike;
import com.yourmode.yourmodebackend.domain.content.repository.ContentLikeRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import com.yourmode.yourmodebackend.domain.content.status.ContentErrorStatus;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentLikeServiceImpl implements ContentLikeService {

    private final ContentLikeRepository contentLikeRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public LikeResponseDto addLike(Integer contentId, Integer userId) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 콘텐츠 존재 여부 확인
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new RestApiException(UserErrorStatus.USER_NOT_FOUND);
        }
        
        // 이미 좋아요를 눌렀는지 확인
        if (contentLikeRepository.existsByContentIdAndUserId(contentId, userId)) {
            throw new RestApiException(ContentErrorStatus.LIKE_ALREADY_EXISTS);
        }
        
        // 좋아요 생성
        ContentLike like = ContentLike.builder()
                .content(content)
                .user(userRepository.findById(userId).orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND)))
                .build();
        
        try {
            ContentLike savedLike = contentLikeRepository.save(like);
            
            // DTO 변환하여 반환
            return LikeResponseDto.builder()
                    .id(savedLike.getId())
                    .createdAt(savedLike.getCreatedAt())
                    .userId(savedLike.getUser().getId())
                    .userName(savedLike.getUser().getName())
                    .contentId(savedLike.getContent().getId())
                    .build();
        } catch (Exception e) {
            throw new RestApiException(ContentErrorStatus.LIKE_ADD_FAILED);
        }
    }

    @Override
    @Transactional
    public void removeLike(Integer contentId, Integer userId) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 콘텐츠 존재 여부 확인
        if (!contentRepository.existsById(contentId)) {
            throw new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND);
        }
        
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new RestApiException(UserErrorStatus.USER_NOT_FOUND);
        }
        
        // 좋아요 찾기
        ContentLike like = contentLikeRepository.findByContentIdAndUserId(contentId, userId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.LIKE_NOT_FOUND));
        
        // 좋아요 삭제
        try {
            contentLikeRepository.delete(like);
        } catch (Exception e) {
            throw new RestApiException(ContentErrorStatus.LIKE_REMOVE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getMyLikesCount(Integer userId) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 사용자 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            throw new RestApiException(UserErrorStatus.USER_NOT_FOUND);
        }
        
        // 사용자가 누른 좋아요 총 개수 조회
        return contentLikeRepository.countByUserId(userId);
    }
}
