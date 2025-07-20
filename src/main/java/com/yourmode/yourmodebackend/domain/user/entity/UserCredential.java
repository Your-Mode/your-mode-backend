package com.yourmode.yourmodebackend.domain.user.entity;

import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "oauth_id")
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    private OAuthProvider oauthProvider;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
