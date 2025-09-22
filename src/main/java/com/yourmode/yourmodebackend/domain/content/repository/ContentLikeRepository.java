package com.yourmode.yourmodebackend.domain.content.repository;

import com.yourmode.yourmodebackend.domain.content.entity.ContentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentLikeRepository extends JpaRepository<ContentLike, Integer> {
    
    /**
     * 특정 콘텐츠의 좋아요 수 조회
     */
    Long countByContentId(Integer contentId);
    
    /**
     * 사용자가 특정 콘텐츠에 좋아요를 눌렀는지 확인
     */
    boolean existsByContentIdAndUserId(Integer contentId, Integer userId);
    
    /**
     * 사용자가 좋아요한 콘텐츠 수 조회
     */
    Long countByUserId(Integer userId);
    
    /**
     * 특정 사용자와 콘텐츠로 좋아요 조회
     */
    Optional<ContentLike> findByContentIdAndUserId(Integer contentId, Integer userId);
}
