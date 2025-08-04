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

    @Column(name = "body_type_name", nullable = false)
    private String bodyTypeName;

    @Column(name = "type_description", columnDefinition = "TEXT")
    private String typeDescription;

    @Column(name = "detailed_features", columnDefinition = "TEXT")
    private String detailedFeatures;

    @Column(name = "attraction_points", columnDefinition = "TEXT")
    private String attractionPoints;

    @Column(name = "recommended_styles", columnDefinition = "TEXT")
    private String recommendedStyles;

    @Column(name = "avoid_styles", columnDefinition = "TEXT")
    private String avoidStyles;

    @Column(name = "styling_fixes", columnDefinition = "TEXT")
    private String stylingFixes;

    @Column(name = "styling_tips", columnDefinition = "TEXT")
    private String stylingTips;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_history_id", nullable = false)
    private SurveyHistory surveyHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "body_type_id")
    private BodyType bodyType;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
