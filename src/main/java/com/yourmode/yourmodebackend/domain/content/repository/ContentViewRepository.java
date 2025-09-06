package com.yourmode.yourmodebackend.domain.content.repository;

import com.yourmode.yourmodebackend.domain.content.entity.ContentView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ContentViewRepository extends JpaRepository<ContentView, Integer> {
    
    /**
     * 특정 콘텐츠의 조회수 조회
     */
    Long countByContentId(Integer contentId);
    
    /**
     * 사용자가 특정 콘텐츠를 조회했는지 확인
     */
    boolean existsByContentIdAndUserId(Integer contentId, Integer userId);
    
    /**
     * 특정 사용자와 콘텐츠로 조회 기록 조회
     */
    Optional<ContentView> findByContentIdAndUserId(Integer contentId, Integer userId);
    
    /**
     * 사용자가 조회한 콘텐츠 수 조회
     */
    Long countByUserId(Integer userId);
    
    /**
     * 조회 시간 업데이트
     */
    @Modifying
    @Query("UPDATE ContentView cv SET cv.viewedAt = :viewedAt WHERE cv.content.id = :contentId AND cv.user.id = :userId")
    int updateViewedAt(@Param("contentId") Integer contentId, @Param("userId") Integer userId, @Param("viewedAt") LocalDateTime viewedAt);
}
