package com.yourmode.yourmodebackend.domain.content.repository;

import com.yourmode.yourmodebackend.domain.content.entity.ContentComment;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentCommentRepository extends JpaRepository<ContentComment, Integer> {
    
    /**
     * 특정 콘텐츠의 댓글 목록을 페이지네이션과 함께 조회
     */
    Page<ContentComment> findByContentIdOrderByCreatedAtDesc(Integer contentId, Pageable pageable);
    
    /**
     * 특정 콘텐츠의 댓글 수 조회
     */
    Long countByContentId(Integer contentId);
    
    /**
     * 사용자가 작성한 댓글 수 조회
     */
    Long countByUserId(Integer userId);
    
    /**
     * 사용자가 댓글을 작성한 컨텐츠 수 조회 (중복 제거)
     */
    @Query("SELECT COUNT(DISTINCT cc.content.id) FROM ContentComment cc WHERE cc.user.id = :userId")
    Long countDistinctContentIdByUserId(@Param("userId") Integer userId);
    
    /**
     * 사용자가 작성한 댓글인지 확인
     */
    boolean existsByIdAndUser(Integer commentId, User user);
    
    /**
     * 특정 댓글을 사용자와 함께 조회
     */
    @Query("SELECT cc FROM ContentComment cc JOIN FETCH cc.user WHERE cc.id = :commentId")
    Optional<ContentComment> findByIdWithUser(@Param("commentId") Integer commentId);
    
    /**
     * 특정 콘텐츠의 댓글을 사용자 정보와 함께 조회
     */
    @Query("SELECT cc FROM ContentComment cc JOIN FETCH cc.user WHERE cc.content.id = :contentId ORDER BY cc.createdAt DESC")
    Page<ContentComment> findByContentIdWithUserOrderByCreatedAtDesc(@Param("contentId") Integer contentId, Pageable pageable);
}
