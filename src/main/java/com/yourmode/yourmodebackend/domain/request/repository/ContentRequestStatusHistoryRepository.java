package com.yourmode.yourmodebackend.domain.request.repository;

import com.yourmode.yourmodebackend.domain.request.entity.ContentRequestStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRequestStatusHistoryRepository extends JpaRepository<ContentRequestStatusHistory, Long> {
}
