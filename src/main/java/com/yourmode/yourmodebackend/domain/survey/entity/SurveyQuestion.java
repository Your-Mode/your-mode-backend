package com.yourmode.yourmodebackend.domain.survey.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "survey_questions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    // 선택지 목록 (양방향 연관관계)
    @OneToMany(mappedBy = "surveyQuestion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyOption> options;
}
