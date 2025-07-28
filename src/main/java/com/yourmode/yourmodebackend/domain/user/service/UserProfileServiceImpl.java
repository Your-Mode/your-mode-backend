package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.UserProfileUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserProfileResponseDto;
import com.yourmode.yourmodebackend.domain.user.entity.UserProfile;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import com.yourmode.yourmodebackend.domain.user.repository.BodyTypeRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserProfileRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yourmode.yourmodebackend.domain.user.entity.UserCredential;
import com.yourmode.yourmodebackend.domain.user.repository.UserCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return UserProfileResponseDto
     * @throws RestApiException ACCESS_DENIED, USER_NOT_FOUND, PROFILE_NOT_FOUND 등
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getMyProfile(Integer userId) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.PROFILE_NOT_FOUND));

        return UserProfileResponseDto.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .gender(profile.getGender())
                .bodyTypeId(profile.getBodyType() != null ? profile.getBodyType().getId() : null)
                .build();
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * @param userId 사용자 ID
     * @param dto 프로필 수정 요청 DTO
     * @return UserProfileResponseDto
     * @throws RestApiException ACCESS_DENIED, USER_NOT_FOUND, PROFILE_NOT_FOUND, BODY_TYPE_NOT_FOUND 등
     */
    @Transactional
    public UserProfileResponseDto updateMyProfile(Integer userId, UserProfileUpdateRequestDto dto) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.PROFILE_NOT_FOUND));

        BodyType bodyType = bodyTypeRepository.findById(dto.getBodyTypeId())
                .orElseThrow(() -> new RestApiException(UserErrorStatus.BODY_TYPE_NOT_FOUND));

        // 사용자 정보 수정
        user.setName(dto.getName());
        user.setPhoneNumber(dto.getPhoneNumber());
        profile.setHeight(dto.getHeight());
        profile.setWeight(dto.getWeight());
        profile.setGender(dto.getGender());
        profile.setBodyType(bodyType);

        return UserProfileResponseDto.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .gender(profile.getGender())
                .bodyTypeId(profile.getBodyType() != null ? profile.getBodyType().getId() : null)
                .build();
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param userId 사용자 ID
     * @param newPassword 새 비밀번호(암호화 전)
     * @throws RestApiException ACCESS_DENIED, USER_NOT_FOUND, CREDENTIAL_NOT_FOUND 등
     */
    @Transactional
    public void updatePassword(Integer userId, String newPassword) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
        UserCredential credential = userCredentialRepository.findByUser(user)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.CREDENTIAL_NOT_FOUND));
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        userCredentialRepository.save(credential);
    }
}

