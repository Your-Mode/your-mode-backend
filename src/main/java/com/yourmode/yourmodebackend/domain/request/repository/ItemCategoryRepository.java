package com.yourmode.yourmodebackend.domain.request.repository;

import com.yourmode.yourmodebackend.domain.request.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer> {

    // 카테고리 이름으로 존재 여부 확인
    boolean existsByName(String name);

    // 이름으로 조회
    Optional<ItemCategory> findByName(String name);
}