package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserCredential;
import com.yourmode.yourmodebackend.domain.user.domain.UserProfile;
import com.yourmode.yourmodebackend.domain.user.domain.UserToken;
import com.yourmode.yourmodebackend.domain.user.dto.*;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.domain.user.mapper.UserMapper;

import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponseDto signUp(LocalSignupRequestDto request) {
        validateDuplicateEmail(request.getEmail());

        // 유저 생성 및 저장
        User user = createAndSaveUser(request);

        // Credential 저장
        saveUserCredential(user.getUserId(), request.getPassword());

        // 프로필 저장
        saveUserProfile(user.getUserId(), request);

        // JWT 발급
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(user.getUserId());
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(user.getUserId());

        saveUserToken(user.getUserId(), refresh.token(), refresh.expiry());

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(user.getName())
                .role(user.getRole())
                .bodyTypeId(request.getBodyTypeId())
                .build();

        return AuthResponseDto.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .user(userInfo)
                .build();
    }

    private void validateDuplicateEmail(String email) {
        if (userMapper.isEmailExists(email)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
        }
    }

    private User createAndSaveUser(LocalSignupRequestDto request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setIsTermsAgreed(request.getIsTermsAgreed());
        user.setIsPrivacyPolicyAgreed(request.getIsPrivacyPolicyAgreed());
        user.setIsMarketingAgreed(request.getIsMarketingAgreed());
        user.setCreatedAt(LocalDateTime.now());

        userMapper.insertUser(user); // user_id는 이 시점에 생성되어야 함 (MyBatis 설정 확인 필요)
        return user;
    }

    private void saveUserCredential(Long userId, String rawPassword) {
        UserCredential credential = new UserCredential();
        credential.setUserId(userId);
        credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        credential.setOauthProvider(OAuthProvider.LOCAL);
        credential.setOauthId(null);

        userMapper.insertUserCredential(credential);
    }

    private void saveUserProfile(Long userId, LocalSignupRequestDto request) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBodyTypeId(request.getBodyTypeId());

        userMapper.insertUserProfile(profile);
    }

    private void saveUserToken(Long userId, String refreshToken, LocalDateTime expiredAt) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiredAt(expiredAt);

        userMapper.insertUserToken(userToken);
    }

    // todo: login, socialLogin, logout, refreshToken, 비밀번호 바꾸기
}
