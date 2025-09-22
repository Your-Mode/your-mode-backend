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
import com.yourmode.yourmodebackend.domain.user.entity.BodyType;
import com.yourmode.yourmodebackend.domain.user.repository.BodyTypeRepository;
import com.yourmode.yourmodebackend.domain.request.entity.ContentRequest;
import com.yourmode.yourmodebackend.domain.request.repository.ContentRequestRepository;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.yourmode.yourmodebackend.domain.content.status.ContentErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.domain.content.dto.request.s3.FileDeleteRequestDto;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final ContentCategoryRepository contentCategoryRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private final ContentRequestRepository contentRequestRepository;
    private final S3Service s3Service;
    // TODO: ContentCategoryRepository 또는 EntityManager 주입 필요(임시 null 처리)

    @Override
    @Transactional
    public ContentDetailResponseDto createContent(ContentCreateRequestDto dto, Integer editorId) {
        if (editorId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        User editor = userRepository.findById(editorId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS));

        Content content = new Content();
        content.setTitle(dto.getTitle());
        content.setMainImgUrl(dto.getMainImgUrl());
        content.setRecommended(dto.isRecommended());
        content.setPublishAt(dto.getPublishAt());
        content.setEditor(editor);
        content.setCreatedAt(java.time.LocalDateTime.now());
        content.setEditedAt(java.time.LocalDateTime.now());

        // ContentRequest 매핑
        if (dto.getContentsRequestId() != null) {
            ContentRequest contentRequest = contentRequestRepository.findById(dto.getContentsRequestId())
                    .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
            content.setContentRequest(contentRequest);
        } else {
            content.setContentRequest(null);
        }

        // BodyType 매핑
        if (dto.getBodyTypeIds() != null && !dto.getBodyTypeIds().isEmpty()) {
            content.setBodyTypes(new HashSet<>(bodyTypeRepository.findAllById(dto.getBodyTypeIds())));
        } else {
            content.setBodyTypes(new HashSet<>());
        }

        // 카테고리 매핑
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            content.setContentCategories(new HashSet<>(contentCategoryRepository.findAllById(dto.getCategoryIds())));
        } else {
            content.setContentCategories(new HashSet<>());
        }

        // ContentBlock 매핑
        if (dto.getBlocks() != null) {
            ArrayList<ContentBlock> blockEntities = new ArrayList<>();
            for (ContentCreateRequestDto.ContentBlockDto blockDto : dto.getBlocks()) {
                ContentBlock block = new ContentBlock();
                block.setBlockType(blockDto.getBlockType());
                block.setContentData(blockDto.getContentData());
                block.setBlockOrder(blockDto.getBlockOrder());
                block.setContent(content);

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

        Content saved = contentRepository.save(content);
        return toDetailDto(saved);
    }

    private ContentDetailResponseDto toDetailDto(Content content) {
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setMainImgUrl(content.getMainImgUrl());
        dto.setRecommended(content.isRecommended());
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
        // 바디타입 매핑
        if (content.getBodyTypes() != null) {
            dto.setBodyTypes(content.getBodyTypes().stream().map(bt -> {
                ContentDetailResponseDto.BodyTypeDto b = new ContentDetailResponseDto.BodyTypeDto();
                b.setId(bt.getId());
                b.setName(bt.getName());
                return b;
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
    @Transactional
    public ContentDetailResponseDto updateContent(Integer contentId, ContentCreateRequestDto dto, Integer editorId) {
        if (editorId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        if (!content.getEditor().getId().equals(editorId)) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        content.setTitle(dto.getTitle());
        content.setMainImgUrl(dto.getMainImgUrl());
        content.setRecommended(dto.isRecommended());
        content.setPublishAt(dto.getPublishAt());
        content.setEditedAt(java.time.LocalDateTime.now());

        // ContentRequest 매핑
        if (dto.getContentsRequestId() != null) {
            ContentRequest contentRequest = contentRequestRepository.findById(dto.getContentsRequestId())
                    .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
            content.setContentRequest(contentRequest);
        } else {
            content.setContentRequest(null);
        }

        // BodyType 매핑
        if (dto.getBodyTypeIds() != null && !dto.getBodyTypeIds().isEmpty()) {
            content.setBodyTypes(new HashSet<>(bodyTypeRepository.findAllById(dto.getBodyTypeIds())));
        } else {
            content.setBodyTypes(new HashSet<>());
        }

        // 카테고리 매핑
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            content.setContentCategories(new HashSet<>(contentCategoryRepository.findAllById(dto.getCategoryIds())));
        } else {
            content.setContentCategories(new HashSet<>());
        }

        // ContentBlock(블록) 전체 삭제 후 재생성 (안전한 방식)
        ArrayList<ContentBlock> newBlocks = new ArrayList<>();
        if (dto.getBlocks() != null) {
            for (ContentCreateRequestDto.ContentBlockDto blockDto : dto.getBlocks()) {
                ContentBlock block = new ContentBlock();
                block.setBlockType(blockDto.getBlockType());
                block.setContentData(blockDto.getContentData());
                block.setBlockOrder(blockDto.getBlockOrder());
                block.setContent(content);

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
                newBlocks.add(block);
            }
        }
        content.setContentBlocks(newBlocks);
        Content saved = contentRepository.save(content);
        return toDetailDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContentDetailResponseDto getContentDetail(Integer contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        return toDetailDto(content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentListResponseDto> getAllContents() {
        List<Content> contents = contentRepository.findAll();
        return contents.stream().map(content -> {
            ContentListResponseDto dto = new ContentListResponseDto();
            dto.setId(content.getId());
            dto.setTitle(content.getTitle());
            dto.setMainImgUrl(content.getMainImgUrl());
            dto.setRecommended(content.isRecommended());
            dto.setPublishAt(content.getPublishAt());
            dto.setCreatedAt(content.getCreatedAt());
            dto.setEditedAt(content.getEditedAt());
            if (content.getContentCategories() != null) {
                dto.setCategories(content.getContentCategories().stream().map(cat -> {
                    ContentListResponseDto.CategoryDto c = new ContentListResponseDto.CategoryDto();
                    c.setId(cat.getId());
                    c.setName(cat.getName());
                    return c;
                }).collect(Collectors.toList()));
            }
            // 바디타입 매핑
            if (content.getBodyTypes() != null) {
                dto.setBodyTypes(content.getBodyTypes().stream().map(bt -> {
                    ContentListResponseDto.BodyTypeDto b = new ContentListResponseDto.BodyTypeDto();
                    b.setId(bt.getId());
                    b.setName(bt.getName());
                    return b;
                }).collect(Collectors.toList()));
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteContent(Integer contentId, Integer editorId) {
        if (editorId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        
        // 권한 확인
        if (!content.getEditor().getId().equals(editorId)) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        // S3에서 이미지 파일들 삭제
        try {
            // 메인 이미지 삭제
            if (content.getMainImgUrl() != null && !content.getMainImgUrl().isEmpty()) {
                FileDeleteRequestDto deleteRequest = FileDeleteRequestDto.builder()
                        .userId(editorId)
                        .fileUrl(content.getMainImgUrl())
                        .build();
                s3Service.deleteFile(deleteRequest);
            }
            
            // 블록 이미지들 삭제
            if (content.getContentBlocks() != null) {
                for (ContentBlock block : content.getContentBlocks()) {
                    if (block.getImages() != null) {
                        for (ContentBlockImage image : block.getImages()) {
                            if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {
                                FileDeleteRequestDto deleteRequest = FileDeleteRequestDto.builder()
                                        .userId(editorId)
                                        .fileUrl(image.getImageUrl())
                                        .build();
                                s3Service.deleteFile(deleteRequest);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // S3 삭제 실패해도 DB 삭제는 진행 (로그 기록 권장)
            System.err.println("Failed to delete S3 files for content " + contentId + ": " + e.getMessage());
        }
        
        // DB에서 컨텐츠 삭제 (Cascade로 관련 데이터도 함께 삭제됨)
        contentRepository.delete(content);
    }
} 