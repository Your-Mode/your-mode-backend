package com.yourmode.yourmodebackend.domain.content.repository;

import com.yourmode.yourmodebackend.domain.content.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Integer> {

    @Query(
            value = "select distinct c from Content c left join c.contentCategories cc where cc.id in :categoryIds",
            countQuery = "select count(distinct c) from Content c left join c.contentCategories cc where cc.id in :categoryIds"
    )
    Page<Content> findByCategoryIds(@Param("categoryIds") List<Integer> categoryIds, Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Content c 
            LEFT JOIN c.contentCategories cc 
            LEFT JOIN c.bodyTypes bt 
            WHERE (:categoryIds IS NULL OR cc.id IN :categoryIds)
            AND (:bodyTypeIds IS NULL OR bt.id IN :bodyTypeIds)
            """)
    Page<Content> findByCategoryIdsAndBodyTypeIds(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("bodyTypeIds") List<Integer> bodyTypeIds,
            Pageable pageable
    );

    // 내 컨텐츠 목록 조회 (특정 사용자가 요청한 ContentRequest에 대응하는 컨텐츠)
    @Query("""
            SELECT DISTINCT c FROM Content c 
            LEFT JOIN c.contentCategories cc 
            LEFT JOIN c.bodyTypes bt 
            WHERE c.contentRequest.user.id = :userId
            AND (:categoryIds IS NULL OR cc.id IN :categoryIds)
            AND (:bodyTypeIds IS NULL OR bt.id IN :bodyTypeIds)
            """)
    Page<Content> findByUserIdAndCategoryIdsAndBodyTypeIds(
            @Param("userId") Integer userId,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("bodyTypeIds") List<Integer> bodyTypeIds,
            Pageable pageable
    );

    // 에디터 컨텐츠 목록 조회 (ContentRequest가 없는 컨텐츠 - 에디터가 자유롭게 작성)
    @Query("""
            SELECT DISTINCT c FROM Content c 
            LEFT JOIN c.contentCategories cc 
            LEFT JOIN c.bodyTypes bt 
            WHERE c.contentRequest IS NULL
            AND (:categoryIds IS NULL OR cc.id IN :categoryIds)
            AND (:bodyTypeIds IS NULL OR bt.id IN :bodyTypeIds)
            """)
    Page<Content> findEditorContentsByCategoryIdsAndBodyTypeIds(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("bodyTypeIds") List<Integer> bodyTypeIds,
            Pageable pageable
    );

    // 맞춤형 컨텐츠 목록 조회 (ContentRequest가 있는 컨텐츠 - 사용자 요청에 의해 작성)
    @Query("""
            SELECT DISTINCT c FROM Content c 
            LEFT JOIN c.contentCategories cc 
            LEFT JOIN c.bodyTypes bt 
            WHERE c.contentRequest IS NOT NULL
            AND (:categoryIds IS NULL OR cc.id IN :categoryIds)
            AND (:bodyTypeIds IS NULL OR bt.id IN :bodyTypeIds)
            """)
    Page<Content> findCustomContentsByCategoryIdsAndBodyTypeIds(
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("bodyTypeIds") List<Integer> bodyTypeIds,
            Pageable pageable
    );


}