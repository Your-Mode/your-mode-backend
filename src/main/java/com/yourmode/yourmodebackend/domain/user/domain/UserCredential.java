package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import lombok.Data;

@Data
public class UserCredential {
    private Long userCredentialId;
    private String passwordHash;      // 일반 로그인 비밀번호 (암호화)
    private String oauthId;           // OAuth 식별자
    private OAuthProvider oauthProvider;  // 'LOCAL', 'KAKAO', 'NAVER' 등

    private Long userId;       // FK → users
}
