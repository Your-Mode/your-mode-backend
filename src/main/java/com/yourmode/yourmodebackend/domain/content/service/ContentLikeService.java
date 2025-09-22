package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.LikeResponseDto;

public interface ContentLikeService {
    
    /**
     * 콘텐츠에 좋아요를 추가합니다.
     * @param contentId 콘텐츠 ID
     * @param userId 사용자 ID
     * @return 추가된 좋아요 정보
     */
    LikeResponseDto addLike(Integer contentId, Integer userId);
    
    /**
     * 콘텐츠의 좋아요를 제거합니다.
     * @param contentId 콘텐츠 ID
     * @param userId 사용자 ID
     */
    void removeLike(Integer contentId, Integer userId);
    
    /**
     * 사용자가 누른 좋아요 총 개수를 조회합니다.
     * @param userId 사용자 ID
     * @return 좋아요 총 개수
     */
    Long getMyLikesCount(Integer userId);
}
