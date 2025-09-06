package com.yourmode.yourmodebackend.domain.content.repository;

import com.yourmode.yourmodebackend.domain.content.entity.ContentLike;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentLikeRepository extends JpaRepository<ContentLike, Integer> {
    
    /**
     * 특정 콘텐츠의 좋아요 목록을 페이지네이션과 함께 조회
     */
    Page<ContentLike> findByContentIdOrderByCreatedAtDesc(Integer contentId, Pageable pageable);
    
    /**
     * 특정 콘텐츠의 좋아요 수 조회
     */
    Long countByContentId(Integer contentId);
    
    /**
     * 사용자가 특정 콘텐츠에 좋아요를 눌렀는지 확인
     */
    boolean existsByContentIdAndUserId(Integer contentId, Integer userId);
    
    /**
     * 사용자가 특정 콘텐츠에 좋아요를 눌렀는지 확인 (User 엔티티로)
     */
    boolean existsByContentIdAndUser(Integer contentId, User user);
    
    /**
     * 특정 콘텐츠의 좋아요를 사용자 정보와 함께 조회
     */
    @Query("SELECT cl FROM ContentLike cl JOIN FETCH cl.user WHERE cl.content.id = :contentId ORDER BY cl.createdAt DESC")
    Page<ContentLike> findByContentIdWithUserOrderByCreatedAtDesc(@Param("contentId") Integer contentId, Pageable pageable);
    
    /**
     * 사용자가 좋아요한 콘텐츠 목록 조회
     */
    @Query("SELECT cl FROM ContentLike cl JOIN FETCH cl.content WHERE cl.user.id = :userId ORDER BY cl.createdAt DESC")
    Page<ContentLike> findByUserIdWithContentOrderByCreatedAtDesc(@Param("userId") Integer userId, Pageable pageable);
    
    /**
     * 사용자가 좋아요한 콘텐츠 수 조회
     */
    Long countByUserId(Integer userId);
    
    /**
     * 특정 사용자와 콘텐츠로 좋아요 조회
     */
    Optional<ContentLike> findByContentIdAndUserId(Integer contentId, Integer userId);
    
    /**
     * 특정 사용자와 콘텐츠로 좋아요 조회 (User 엔티티로)
     */
    Optional<ContentLike> findByContentIdAndUser(Integer contentId, User user);
}
