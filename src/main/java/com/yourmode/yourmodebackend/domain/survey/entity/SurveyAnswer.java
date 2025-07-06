package com.yourmode.yourmodebackend.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_answers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 설문 이력
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_history_id", nullable = false)
    private SurveyHistory surveyHistory;

    // 질문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id", nullable = false)
    private SurveyQuestion surveyQuestion;

    // 선택지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_option_id", nullable = false)
    private SurveyOption surveyOption;
}
