package com.yourmode.yourmodebackend.domain.survey.entity;

import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "survey_options")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // 체형 타입
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_type_id", nullable = false)
    private BodyType bodyType;

    // 질문
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_question_id", nullable = false)
    private SurveyQuestion surveyQuestion;
}
