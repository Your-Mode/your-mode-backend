package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewCountResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.UserViewCountResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.entity.ContentView;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentViewRepository;
import com.yourmode.yourmodebackend.domain.content.status.ContentErrorStatus;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContentViewServiceImpl implements ContentViewService {

    private final ContentViewRepository contentViewRepository;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ContentViewResponseDto addOrUpdateView(Integer contentId, Integer userId) {
        // 콘텐츠 존재 확인
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS));

        LocalDateTime viewedAt = LocalDateTime.now();

        // 이미 조회한 기록이 있는지 확인
        ContentView existingView = contentViewRepository.findByContentIdAndUserId(contentId, userId)
                .orElse(null);

        if (existingView != null) {
            // 이미 조회한 경우 시간 업데이트
            contentViewRepository.updateViewedAt(contentId, userId, viewedAt);
        } else {
            // 새로운 조회 기록 생성
            ContentView contentView = ContentView.builder()
                    .content(content)
                    .user(user)
                    .viewedAt(viewedAt)
                    .build();
            contentViewRepository.save(contentView);
        }
        
        return ContentViewResponseDto.builder()
                .userId(userId)
                .contentId(contentId)
                .viewedAt(viewedAt)
                .message("조회수가 성공적으로 추가되었습니다.")
                .build();
    }

    @Override
    public ContentViewCountResponseDto getViewCount(Integer contentId) {
        Long count = contentViewRepository.countByContentId(contentId);
        Long viewCount = count != null ? count : 0L;
        
        return ContentViewCountResponseDto.builder()
                .contentId(contentId)
                .viewCount(viewCount)
                .build();
    }

    @Override
    public UserViewCountResponseDto getUserViewCount(Integer userId) {
        Long count = contentViewRepository.countByUserId(userId);
        Long viewedContentsCount = count != null ? count : 0L;
        
        return UserViewCountResponseDto.builder()
                .userId(userId)
                .viewedContentsCount(viewedContentsCount)
                .build();
    }
}
