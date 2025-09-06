package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.UserProfileUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.MyPageComponentResponseDto;
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
import com.yourmode.yourmodebackend.domain.content.service.ContentQueryService;
import com.yourmode.yourmodebackend.domain.content.service.ContentViewService;
import com.yourmode.yourmodebackend.domain.content.service.ContentLikeService;
import com.yourmode.yourmodebackend.domain.content.repository.ContentCommentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContentQueryService contentQueryService;
    private final ContentViewService contentViewService;
    private final ContentLikeService contentLikeService;
    private final ContentCommentRepository contentCommentRepository;

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
                .email(user.getEmail())
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
     * @throws RestApiException ACCESS_DENIED, USER_NOT_FOUND, PROFILE_NOT_FOUND, BODY_TYPE_NOT_FOUND, DUPLICATE_PHONE_NUMBER 등
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

        // 전화번호 중복 체크 (다른 사용자가 이미 사용 중인 경우)
        if (!dto.getPhoneNumber().equals(user.getPhoneNumber()) && 
            userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_PHONE_NUMBER);
        }

        // 바디타입 존재 여부 체크
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
     * @param currentPassword 현재 비밀번호(암호화 전)
     * @param newPassword 새 비밀번호(암호화 전)
     * @throws RestApiException ACCESS_DENIED, USER_NOT_FOUND, CREDENTIAL_NOT_FOUND, INVALID_PASSWORD_FORMAT, INVALID_CURRENT_PASSWORD 등
     */
    @Transactional
    public void updatePassword(Integer userId, String currentPassword, String newPassword) {
        if (userId == null) {
            throw new RestApiException(UserErrorStatus.ACCESS_DENIED);
        }
        
        // 새 비밀번호 유효성 검증
        validatePassword(newPassword);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
        UserCredential credential = userCredentialRepository.findByUser(user)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.CREDENTIAL_NOT_FOUND));
        
        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, credential.getPasswordHash())) {
            throw new RestApiException(UserErrorStatus.INVALID_CURRENT_PASSWORD);
        }
        
        // 새 비밀번호로 업데이트
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        userCredentialRepository.save(credential);
    }

    /**
     * 비밀번호 유효성을 검증합니다.
     * @param password 검증할 비밀번호
     * @throws RestApiException INVALID_PASSWORD_FORMAT
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new RestApiException(UserErrorStatus.INVALID_PASSWORD_FORMAT);
        }
        
        // 영문, 숫자, 특수문자 포함 여부 체크
        boolean hasLetter = Pattern.compile("[a-zA-Z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]").matcher(password).find();
        
        if (!hasLetter || !hasDigit || !hasSpecial) {
            throw new RestApiException(UserErrorStatus.INVALID_PASSWORD_FORMAT);
        }
    }

    @Override
    public MyPageComponentResponseDto getMyPageComponent(Integer userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
        
        // 프로필 정보 조회
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.PROFILE_NOT_FOUND));

        // 각종 카운트 조회
        Long customContentsCount = getCustomContentsCount(userId);
        Long viewedContentsCount = getViewedContentsCount(userId);
        Long likedContentsCount = getLikedContentsCount(userId);
        Long commentedContentsCount = getCommentedContentsCount(userId);
        Long myCommentsCount = getMyCommentsCount(userId);

        return MyPageComponentResponseDto.builder()
                .email(user.getEmail())
                .bodyTypeId(profile.getBodyType() != null ? profile.getBodyType().getId() : null)
                .customContentsCount(customContentsCount)
                .viewedContentsCount(viewedContentsCount)
                .likedContentsCount(likedContentsCount)
                .commentedContentsCount(commentedContentsCount)
                .myCommentsCount(myCommentsCount)
                .build();
    }

    private Long getCustomContentsCount(Integer userId) {
        // 맞춤형 컨텐츠 수 조회 (ContentRequest가 있는 컨텐츠)
        try {
            return contentQueryService.getCustomContents(null, null, 
                org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long getViewedContentsCount(Integer userId) {
        // 조회한 컨텐츠 수 조회
        try {
            return contentViewService.getUserViewCount(userId).getViewedContentsCount();
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long getLikedContentsCount(Integer userId) {
        // 좋아요한 컨텐츠 수 조회
        try {
            return contentLikeService.getMyLikesCount(userId);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long getCommentedContentsCount(Integer userId) {
        // 댓글 단 컨텐츠 수 조회 (사용자가 댓글을 작성한 컨텐츠의 개수)
        try {
            return contentCommentRepository.countDistinctContentIdByUserId(userId);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long getMyCommentsCount(Integer userId) {
        // 사용자가 작성한 댓글 수 조회
        try {
            return contentCommentRepository.countByUserId(userId);
        } catch (Exception e) {
            return 0L;
        }
    }
}

