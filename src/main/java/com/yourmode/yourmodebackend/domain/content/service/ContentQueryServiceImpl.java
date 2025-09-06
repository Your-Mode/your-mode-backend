package com.yourmode.yourmodebackend.domain.content.service;

import com.yourmode.yourmodebackend.domain.content.dto.response.ContentDetailResponseDto;
import com.yourmode.yourmodebackend.domain.content.dto.response.ContentListResponseDto;
import com.yourmode.yourmodebackend.domain.content.entity.Content;
import com.yourmode.yourmodebackend.domain.content.repository.ContentRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentLikeRepository;
import com.yourmode.yourmodebackend.domain.content.repository.ContentCommentRepository;
import com.yourmode.yourmodebackend.domain.content.status.ContentErrorStatus;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentQueryServiceImpl implements ContentQueryService {

    private final ContentRepository contentRepository;
    private final ContentLikeRepository contentLikeRepository;
    private final ContentCommentRepository contentCommentRepository;

    @Override
    public Page<ContentListResponseDto> getContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        Page<Content> page;
        
        // 둘 다 null이거나 빈 리스트인 경우 전체 조회
        if ((categoryIds == null || categoryIds.isEmpty()) && (bodyTypeIds == null || bodyTypeIds.isEmpty())) {
            page = contentRepository.findAll(pageable);
        } else {
            // 복합 조건 검색
            page = contentRepository.findByCategoryIdsAndBodyTypeIds(categoryIds, bodyTypeIds, pageable);
        }
        
        return page.map(this::toListDto);
    }

    @Override
    public ContentDetailResponseDto getContentDetail(Integer contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RestApiException(ContentErrorStatus.CONTENT_NOT_FOUND));
        return toDetailDto(content);
    }

    @Override
    public Page<ContentListResponseDto> getMyContents(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        if (userId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        Page<Content> page = contentRepository.findByUserIdAndCategoryIdsAndBodyTypeIds(userId, categoryIds, bodyTypeIds, pageable);
        return page.map(this::toListDto);
    }

    @Override
    public Page<ContentListResponseDto> getEditorContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        Page<Content> page = contentRepository.findEditorContentsByCategoryIdsAndBodyTypeIds(categoryIds, bodyTypeIds, pageable);
        return page.map(this::toListDto);
    }

    @Override
    public Page<ContentListResponseDto> getCustomContents(List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        Page<Content> page = contentRepository.findCustomContentsByCategoryIdsAndBodyTypeIds(categoryIds, bodyTypeIds, pageable);
        return page.map(this::toListDto);
    }

    @Override
    public Page<ContentListResponseDto> getContentsByUserComments(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        if (userId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        Page<Content> page = contentRepository.findContentsByUserComments(userId, categoryIds, bodyTypeIds, pageable);
        return page.map(this::toListDto);
    }

    @Override
    public Page<ContentListResponseDto> getContentsByUserLikes(Integer userId, List<Integer> categoryIds, List<Integer> bodyTypeIds, Pageable pageable) {
        if (userId == null) {
            throw new RestApiException(ContentErrorStatus.FORBIDDEN_CONTENT_ACCESS);
        }
        
        Page<Content> page = contentRepository.findContentsByUserLikes(userId, categoryIds, bodyTypeIds, pageable);
        return page.map(this::toListDto);
    }

    private ContentListResponseDto toListDto(Content content) {
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
        if (content.getBodyTypes() != null) {
            dto.setBodyTypes(content.getBodyTypes().stream().map(bt -> {
                ContentListResponseDto.BodyTypeDto b = new ContentListResponseDto.BodyTypeDto();
                b.setId(bt.getId());
                b.setName(bt.getName());
                return b;
            }).collect(Collectors.toList()));
        }
        
        // 좋아요 수와 댓글 수 추가
        dto.setLikeCount(getLikeCount(content.getId()));
        dto.setCommentCount(getCommentCount(content.getId()));
        
        return dto;
    }

    private ContentDetailResponseDto toDetailDto(Content content) {
        // 재사용을 위해 ContentServiceImpl의 변환 로직을 간단히 복사
        ContentDetailResponseDto dto = new ContentDetailResponseDto();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setMainImgUrl(content.getMainImgUrl());
        dto.setRecommended(content.isRecommended());
        dto.setPublishAt(content.getPublishAt());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setEditedAt(content.getEditedAt());
        if (content.getContentCategories() != null) {
            dto.setCategories(content.getContentCategories().stream().map(cat -> {
                ContentDetailResponseDto.CategoryDto c = new ContentDetailResponseDto.CategoryDto();
                c.setId(cat.getId());
                c.setName(cat.getName());
                return c;
            }).collect(Collectors.toList()));
        }
        if (content.getBodyTypes() != null) {
            dto.setBodyTypes(content.getBodyTypes().stream().map(bt -> {
                ContentDetailResponseDto.BodyTypeDto b = new ContentDetailResponseDto.BodyTypeDto();
                b.setId(bt.getId());
                b.setName(bt.getName());
                return b;
            }).collect(Collectors.toList()));
        }
        if (content.getContentBlocks() != null) {
            dto.setBlocks(content.getContentBlocks().stream().map(block -> {
                ContentDetailResponseDto.ContentBlockDto b = new ContentDetailResponseDto.ContentBlockDto();
                b.setBlockType(block.getBlockType());
                b.setContentData(block.getContentData());
                b.setBlockOrder(block.getBlockOrder());
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
        
        // 좋아요 수와 댓글 수 추가
        dto.setLikeCount(getLikeCount(content.getId()));
        dto.setCommentCount(getCommentCount(content.getId()));
        
        return dto;
    }

    /**
     * 콘텐츠의 좋아요 수를 조회합니다.
     */
    private Long getLikeCount(Integer contentId) {
        Long count = contentLikeRepository.countByContentId(contentId);
        return count != null ? count : 0L;
    }

    /**
     * 콘텐츠의 댓글 수를 조회합니다.
     */
    private Long getCommentCount(Integer contentId) {
        Long count = contentCommentRepository.countByContentId(contentId);
        return count != null ? count : 0L;
    }
}


