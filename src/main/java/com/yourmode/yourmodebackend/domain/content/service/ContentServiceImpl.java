package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.ContentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentRepository contentRepository;

    @Override
    public ContentDetailResponseDto createContent(ContentCreateRequestDto dto, Integer editorId) {
        // TODO: implement entity creation, mapping, and save
        return null;
    }

    @Override
    public ContentDetailResponseDto updateContent(Integer contentId, ContentCreateRequestDto dto, Integer editorId) {
        // TODO: implement entity update, mapping, and save
        return null;
    }

    @Override
    public ContentDetailResponseDto getContentDetail(Integer contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new IllegalArgumentException("Content not found: " + contentId));
        // TODO: map entity to ContentDetailResponseDto
        return null;
    }

    @Override
    public List<ContentListResponseDto> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        // TODO: map entities to ContentListResponseDto
        return List.of();
    }
} 