package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.request.ContentCreateRequestDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.entity.ContentBlock;
import com.yourmode.yourmodebackend.domain.content.entity.ContentBlockImage;
import com.yourmode.yourmodebackend.domain.content.entity.ContentBlockStyle;
import com.yourmode.yourmodebackend.domain.content.entity.ContentCategory;
import com.yourmode.yourmodebackend.domain.content.repository.ContentCategoryRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    // TODO: ContentCategoryRepository 또는 EntityManager 주입 필요(임시 null 처리)

    @Override
    @Transactional
    public ContentDetailResponseDto createContent(ContentCreateRequestDto dto, Integer editorId) {
        // 1. 에디터 조회
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new IllegalArgumentException("Editor not found: " + editorId));

        // 2. Content 엔티티 생성 및 매핑
        Content content = new Content();
        content.setTitle(dto.getTitle());
        content.setMainImgUrl(dto.getMainImgUrl());
        content.setRecommended(dto.isRecommended());
        content.setPublishType(dto.getPublishType());
        content.setPublishAt(dto.getPublishAt());
        content.setEditor(editor);
        content.setCreatedAt(java.time.LocalDateTime.now());
        content.setEditedAt(java.time.LocalDateTime.now());

        // 3. 카테고리 매핑
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            HashSet<ContentCategory> categories = new HashSet<>(contentCategoryRepository.findAllById(dto.getCategoryIds()));
            content.setContentCategories(categories);
        } else {
            content.setContentCategories(new HashSet<>());
        }

        // 4. ContentBlock 매핑
        if (dto.getBlocks() != null) {
            ArrayList<ContentBlock> blockEntities = new ArrayList<>();
            for (ContentCreateRequestDto.ContentBlockDto blockDto : dto.getBlocks()) {
                ContentBlock block = new ContentBlock();
                block.setBlockType(blockDto.getBlockType());
                block.setContentData(blockDto.getContentData());
                block.setBlockOrder(blockDto.getBlockOrder());
                block.setContent(content); // 양방향 매핑

                // 스타일 매핑
                if (blockDto.getStyle() != null) {
                    ContentBlockStyle style = new ContentBlockStyle();
                    style.setFontFamily(blockDto.getStyle().getFontFamily());
                    style.setFontSize(blockDto.getStyle().getFontSize());
                    style.setFontWeight(blockDto.getStyle().getFontWeight());
                    style.setTextColor(blockDto.getStyle().getTextColor());
                    style.setBackgroundColor(blockDto.getStyle().getBackgroundColor());
                    style.setTextAlign(blockDto.getStyle().getTextAlign());
                    style.setContentBlock(block);
                    block.setContentBlockStyle(style);
                }

                // 이미지 매핑
                if (blockDto.getImages() != null) {
                    ArrayList<ContentBlockImage> imageEntities = new ArrayList<>();
                    for (ContentCreateRequestDto.ContentBlockImageDto imageDto : blockDto.getImages()) {
                        ContentBlockImage image = new ContentBlockImage();
                        image.setImageUrl(imageDto.getImageUrl());
                        image.setImageOrder(imageDto.getImageOrder());
                        image.setContentBlock(block);
                        imageEntities.add(image);
                    }
                    block.setImages(imageEntities);
                }
                blockEntities.add(block);
            }
            content.setContentBlocks(blockEntities);
        }

        // 5. 저장
        Content saved = contentRepository.save(content);
        return toDetailDto(saved);
    }

    private ContentDetailResponseDto toDetailDto(Content content) {
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setMainImgUrl(content.getMainImgUrl());
        dto.setRecommended(content.isRecommended());
        dto.setPublishType(content.getPublishType());
        dto.setPublishAt(content.getPublishAt());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setEditedAt(content.getEditedAt());
        // 카테고리 매핑
        if (content.getContentCategories() != null) {
            dto.setCategories(content.getContentCategories().stream().map(cat -> {
                ContentDetailResponseDto.CategoryDto c = new ContentDetailResponseDto.CategoryDto();
                c.setId(cat.getId());
                c.setName(cat.getName());
                return c;
            }).collect(Collectors.toList()));
        }
        // 블록 매핑
        if (content.getContentBlocks() != null) {
            dto.setBlocks(content.getContentBlocks().stream().map(block -> {
                ContentDetailResponseDto.ContentBlockDto b = new ContentDetailResponseDto.ContentBlockDto();
                b.setBlockType(block.getBlockType());
                b.setContentData(block.getContentData());
                b.setBlockOrder(block.getBlockOrder());
                // 스타일
                if (block.getContentBlockStyle() != null) {
                    ContentDetailResponseDto.ContentBlockStyleDto style = new ContentDetailResponseDto.ContentBlockStyleDto();
                    style.setFontFamily(block.getContentBlockStyle().getFontFamily());
                    style.setFontSize(block.getContentBlockStyle().getFontSize());
                    style.setFontWeight(block.getContentBlockStyle().getFontWeight());
                    style.setTextColor(block.getContentBlockStyle().getTextColor());
                    style.setBackgroundColor(block.getContentBlockStyle().getBackgroundColor());
                    style.setTextAlign(block.getContentBlockStyle().getTextAlign());
                    b.setStyle(style);
                }
                // 이미지
                if (block.getImages() != null) {
                    b.setImages(block.getImages().stream().map(img -> {
                        ContentDetailResponseDto.ContentBlockImageDto i = new ContentDetailResponseDto.ContentBlockImageDto();
                        i.setImageUrl(img.getImageUrl());
                        i.setImageOrder(img.getImageOrder());
                        return i;
                    }).collect(Collectors.toList()));
                }
                return b;
            }).collect(Collectors.toList()));
        }
        return dto;
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