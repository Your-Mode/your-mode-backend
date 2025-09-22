package com.yourmode.yourmodebackend.domain.survey.repository;

import com.yourmode.yourmodebackend.domain.survey.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Integer> {
    @EntityGraph(attributePaths = "options")
    List<SurveyQuestion> findAllByOrderByOrderNumberAsc();
} 