package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.CommonSignupRequest;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.entity.*;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.domain.user.repository.*;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserTokenRepository userTokenRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoDto buildUserInfoDto(User user) {
        return UserInfoDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isNewUser(false)  // 기존 회원으로 간주
                .build();
    }

    @Override
    @Transactional
    public User createAndSaveUser(CommonSignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setTermsAgreed(request.getIsTermsAgreed());
        user.setPrivacyPolicyAgreed(request.getIsPrivacyPolicyAgreed());
        user.setMarketingAgreed(request.getIsMarketingAgreed());
        user.setCreatedAt(LocalDateTime.now());

        try {
            userRepository.save(user); // save 시 자동으로 userId 세팅됨
        } catch (DataIntegrityViolationException e) {
            String message = e.getRootCause() != null ? e.getRootCause().getMessage() : "";
            if (message.contains("phone_number")) {
                throw new RestApiException(UserErrorStatus.DUPLICATE_PHONE_NUMBER);
            } else if (message.contains("email")) {
                throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
            } else {
                throw new RestApiException(UserErrorStatus.DB_INSERT_FAILED);
            }
        }

        return user;
    }

    @Override
    @Transactional
    public void saveUserCredential(User user, String rawPassword, OAuthProvider provider, String oauthId) {
        UserCredential credential = new UserCredential();
        credential.setUser(user); // userId 대신 User 객체 전체 설정

        if (rawPassword != null) {
            credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else {
            credential.setPasswordHash(null);
        }

        credential.setOauthProvider(provider);
        credential.setOauthId(oauthId);

        user.setCredential(credential);

        try {
            userCredentialRepository.save(credential);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_CREDENTIAL_INSERT_FAILED);
        }
    }

    @Override
    @Transactional
    public void saveUserProfile(User user, CommonSignupRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUser(user); // 연관 관계 설정
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());

        BodyType bodyType = bodyTypeRepository.findById(request.getBodyTypeId())
                .orElseThrow(() -> new RestApiException(UserErrorStatus.BODY_TYPE_NOT_FOUND));

        profile.setBodyType(bodyType);

        user.setProfile(profile);

        try {
            userProfileRepository.save(profile);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_PROFILE_INSERT_FAILED);
        }
    }

    @Override
    @Transactional
    public void saveUserToken(User user, String refreshToken, LocalDateTime expiredAt) {
        // 기존 토큰이 있으면 update, 없으면 insert
        UserToken userToken = userTokenRepository.findByUserId(user.getId())
            .orElse(null);
        if (userToken == null) {
            userToken = new UserToken();
            userToken.setUser(user);
        }
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiredAt(expiredAt);
        try {
            userTokenRepository.save(userToken);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_TOKEN_INSERT_FAILED);
        }
    }

    @Override
    public void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
        }
    }
}
