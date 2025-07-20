package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.UserContentRequestSummaryDto;
import java.util.List;

public interface UserContentRequestService {
    ContentRequestResponseDto createContentRequest(ContentRequestCreateDto dto, Integer userId);
    List<UserContentRequestSummaryDto> getRequestsByUserId(Integer userId);
    UserContentRequestDetailDto getContentRequestById(Integer id, Integer userId);
} 