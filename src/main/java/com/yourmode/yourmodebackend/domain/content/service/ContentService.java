package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.ContentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import java.util.List;

public interface ContentService {
    ContentDetailResponseDto createContent(ContentCreateRequestDto dto, Integer editorId);
    ContentDetailResponseDto updateContent(Integer contentId, ContentCreateRequestDto dto, Integer editorId);
    ContentDetailResponseDto getContentDetail(Integer contentId);
    List<ContentListResponseDto> getAllContents();
    void deleteContent(Integer contentId, Integer editorId);
}