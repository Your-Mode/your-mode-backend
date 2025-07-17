package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestSummaryDto;
import java.util.List;

public interface UserContentRequestService {
    ContentRequestResponseDto createContentRequest(ContentRequestCreateDto dto, Long userId);
    List<UserContentRequestSummaryDto> getRequestsByUserId(Long userId);
    UserContentRequestDetailDto getContentRequestById(Long id, Long userId);
} 