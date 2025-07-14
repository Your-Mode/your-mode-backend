package com.yourmode.yourmodebackend.domain.request.repository;

import com.yourmode.yourmodebackend.domain.request.entity.ContentRequest;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRequestRepository extends JpaRepository<ContentRequest, Long> {
    List<ContentRequest> findAllByUserId(Long userId);
    List<ContentRequest> findAllByOrderByCreatedAtDesc();

    @Query("SELECT cr FROM ContentRequest cr " +
            "JOIN FETCH cr.user u " +
            "LEFT JOIN FETCH u.profile p " +
            "LEFT JOIN FETCH cr.itemCategories ic " +
            "LEFT JOIN FETCH cr.statusHistories sh " +
            "LEFT JOIN FETCH sh.editor e " +
            "WHERE cr.id = :id")
    Optional<ContentRequest> findByIdWithUserAndProfile(@Param("id") Long id);

}
