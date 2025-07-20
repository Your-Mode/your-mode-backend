package com.yourmode.yourmodebackend.domain.request.entity;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "content_requests")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "body_feature", length = 200)
    private String bodyFeature;

    @Column(length = 200)
    private String situation;

    @Column(name = "recommended_style", length = 200)
    private String recommendedStyle;

    @Column(name = "avoided_style", length = 200)
    private String avoidedStyle;

    @Column
    private Integer budget;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    // user_id FK → users(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // status_id FK → request_status_codes(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private RequestStatusCode status;

    // N:M content_requests_item_categories
    @ManyToMany
    @JoinTable(
            name = "content_requests_item_categories", // 중간 테이블명
            joinColumns = @JoinColumn(name = "content_request_id"), // 현재 엔티티의 FK
            inverseJoinColumns = @JoinColumn(name = "item_category_id") // 상대 엔티티의 FK
    )
    @Builder.Default
    private Set<ItemCategory> itemCategories = new HashSet<>();

    // DB에 저장되기 전
    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "contentRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContentRequestStatusHistory> statusHistories = new ArrayList<>();

}
