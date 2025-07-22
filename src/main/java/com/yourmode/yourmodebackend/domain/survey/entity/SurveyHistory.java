package com.yourmode.yourmodebackend.domain.survey.entity;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "survey_histories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 해당 설문 이력에 대한 답변들
    @OneToMany(mappedBy = "surveyHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();

    // 설문 결과
    @OneToMany(mappedBy = "surveyHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResult> results;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
