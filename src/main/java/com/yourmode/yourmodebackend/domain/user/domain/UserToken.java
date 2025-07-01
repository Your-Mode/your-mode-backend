package com.yourmode.yourmodebackend.domain.user.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserToken {
    private Long tokenId;
    private String refreshToken;
    private LocalDateTime expiredAt;

    private Long userId; // FK → users
}