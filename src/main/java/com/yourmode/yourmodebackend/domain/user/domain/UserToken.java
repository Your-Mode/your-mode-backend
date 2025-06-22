package com.yourmode.yourmodebackend.domain.user.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserToken {
    private Long tokenId;
    private String refreshToken;
    private LocalDateTime expiredAt;

    private Long userId; // FK → users
}