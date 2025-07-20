package com.yourmode.yourmodebackend.domain.user.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void updateToken(String refreshToken, LocalDateTime expiredAt) {
        this.refreshToken = refreshToken;
        this.expiredAt = expiredAt;
    }
}
