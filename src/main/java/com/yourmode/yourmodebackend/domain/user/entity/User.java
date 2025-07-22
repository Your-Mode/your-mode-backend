package com.yourmode.yourmodebackend.domain.user.entity;

import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_terms_agreed")
    private boolean isTermsAgreed;

    @Column(name = "is_privacy_policy_agreed")
    private boolean isPrivacyPolicyAgreed;

    @Column(name = "is_marketing_agreed")
    private boolean isMarketingAgreed;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredential credential;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserToken userToken;
}

