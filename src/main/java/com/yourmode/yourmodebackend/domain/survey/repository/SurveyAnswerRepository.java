package com.yourmode.yourmodebackend.domain.survey.repository;

import com.yourmode.yourmodebackend.domain.survey.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Integer> {
} 