package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private Boolean isTermsAgreed = false;
    private Boolean isPrivacyPolicyAgreed = false;
    private Boolean isMarketingAgreed = false;
    private LocalDateTime createdAt;
}
