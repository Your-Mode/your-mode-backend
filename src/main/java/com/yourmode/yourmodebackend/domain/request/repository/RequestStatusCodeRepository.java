package com.yourmode.yourmodebackend.domain.request.repository;

import com.yourmode.yourmodebackend.domain.request.entity.RequestStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestStatusCodeRepository extends JpaRepository<RequestStatusCode, Long> {

    // 상태 이름으로 조회 (예: "신청 접수")
    Optional<RequestStatusCode> findByCodeName(String codeName);
}
