package com.yourmode.yourmodebackend.domain.request.service;

import com.yourmode.yourmodebackend.domain.request.dto.user.request.ContentRequestCreateDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestDetailDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestResponseDto;
import com.yourmode.yourmodebackend.domain.request.dto.user.response.ContentRequestSummaryDto;

import java.util.List;

public interface ContentRequestService {

    ContentRequestResponseDto createContentRequest(ContentRequestCreateDto dto, Long userId);

    List<ContentRequestSummaryDto> getRequestsByUserId(Long userId);

    ContentRequestDetailDto getContentRequestById(Integer id, Long userId);

    // 필요한 경우 다른 메서드 선언...
}
