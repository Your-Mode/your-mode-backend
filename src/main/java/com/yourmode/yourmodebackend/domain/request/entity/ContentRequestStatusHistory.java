package com.yourmode.yourmodebackend.domain.request.entity;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_request_status_histories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    // FK → content_requests(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_request_id", nullable = false)
    private ContentRequest contentRequest;

    // FK → request_status_codes(id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private RequestStatusCode status;

    // FK → users(id) (editor_id, nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editor_id")
    private User editor;

    @PrePersist
    public void prePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
    }
}
