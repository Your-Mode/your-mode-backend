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
}