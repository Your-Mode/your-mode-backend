package com.yourmode.yourmodebackend.domain.user.repository;


import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BodyTypeRepository extends JpaRepository<BodyType, Integer> {
    Optional<BodyType> findByName(String name);
}