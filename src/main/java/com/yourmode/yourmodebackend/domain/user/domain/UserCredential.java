package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {
    private Long userCredentialId;
    private String passwordHash;      // 일반 로그인 비밀번호 (암호화)
    private String oauthId;           // OAuth 식별자
    private OAuthProvider oauthProvider;  // 'LOCAL', 'KAKAO' 등

    private Long userId;       // FK → users
}
