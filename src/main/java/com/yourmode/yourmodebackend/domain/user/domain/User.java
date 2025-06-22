package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private Boolean isMarketingAgreed = false;
    private LocalDateTime createdAt;
}
