package com.yourmode.yourmodebackend.domain.survey.repository;

import com.yourmode.yourmodebackend.domain.survey.entity.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Integer> {
    
    @Query("SELECT sr FROM SurveyResult sr WHERE sr.surveyHistory.user.id = :userId ORDER BY sr.createdAt DESC")
    List<SurveyResult> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    @Query("SELECT sr FROM SurveyResult sr WHERE sr.surveyHistory.id = :historyId")
    List<SurveyResult> findAllBySurveyHistoryId(@Param("historyId") Integer historyId);
} 