package com.yourmode.yourmodebackend.domain.survey.repository;

import com.yourmode.yourmodebackend.domain.survey.entity.SurveyHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyHistoryRepository extends JpaRepository<SurveyHistory, Long> {
    @EntityGraph(attributePaths = {"answers.surveyQuestion", "answers.surveyOption"})
    List<SurveyHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
} 