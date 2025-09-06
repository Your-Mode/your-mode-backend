package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewCountResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentViewResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.UserViewCountResponseDto;

public interface ContentViewService {
    
    /**
     * 콘텐츠 조회수 추가 또는 업데이트
     * 이미 조회한 경우 시간을 업데이트
     * @return 조회수 추가 결과 DTO
     */
    ContentViewResponseDto addOrUpdateView(Integer contentId, Integer userId);
    
    /**
     * 특정 콘텐츠의 조회수 조회
     * @return 조회수 조회 결과 DTO
     */
    ContentViewCountResponseDto getViewCount(Integer contentId);
    
    /**
     * 사용자가 조회한 콘텐츠 수 조회
     * @return 사용자 조회수 조회 결과 DTO
     */
    UserViewCountResponseDto getUserViewCount(Integer userId);
}
