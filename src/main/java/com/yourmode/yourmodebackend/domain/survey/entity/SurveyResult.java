package com.yourmode.yourmodebackend.domain.survey.entity;

import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "survey_results")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_history_id", nullable = false)
    private SurveyHistory surveyHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_type_id", nullable = false)
    private BodyType bodyType;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
