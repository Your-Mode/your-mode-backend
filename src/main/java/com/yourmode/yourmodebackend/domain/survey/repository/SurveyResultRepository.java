package com.yourmode.yourmodebackend.domain.survey.repository;

import com.yourmode.yourmodebackend.domain.survey.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
} 