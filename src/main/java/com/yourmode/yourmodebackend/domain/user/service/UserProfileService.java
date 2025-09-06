package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.UserProfileUpdateRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserProfileResponseDto;

public interface UserProfileService {
    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return UserProfileResponseDto
     */
    UserProfileResponseDto getMyProfile(Integer userId);

    /**
     * 현재 로그인한 사용자의 프로필 정보를 수정합니다.
     * @param userId 사용자 ID
     * @param dto 프로필 수정 요청 DTO
     * @return UserProfileResponseDto
     */
    UserProfileResponseDto updateMyProfile(Integer userId, UserProfileUpdateRequestDto dto);

    /**
     * 사용자의 비밀번호를 변경합니다.
     * @param userId 사용자 ID
     * @param currentPassword 현재 비밀번호(암호화 전)
     * @param newPassword 새 비밀번호(암호화 전)
     */
    void updatePassword(Integer userId, String currentPassword, String newPassword);
}
