package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentQueryService {
    Page<ContentListResponseDto> getContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable);
    ContentDetailResponseDto getContentDetail(Integer contentId);
}


