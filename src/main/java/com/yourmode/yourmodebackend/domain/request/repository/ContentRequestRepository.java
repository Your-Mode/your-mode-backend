package com.yourmode.yourmodebackend.domain.request.repository;

import com.yourmode.yourmodebackend.domain.request.entity.ContentRequest;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRequestRepository extends JpaRepository<ContentRequest, Integer> {
    List<ContentRequest> findAllByUserId(Long userId);
}
