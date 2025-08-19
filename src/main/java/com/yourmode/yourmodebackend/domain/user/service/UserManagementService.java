package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.CommonSignupRequest;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;

public interface UserManagementService {
    UserInfoDto buildUserInfoDto(User user);
    User createAndSaveUser(CommonSignupRequest request);
    void saveUserCredential(User user, String rawPassword, OAuthProvider provider, String oauthId);
    void saveUserProfile(User user, CommonSignupRequest request);
    void saveUserToken(User user, String refreshToken, java.time.LocalDateTime expiredAt);
    void validateDuplicateEmail(String email);
}
