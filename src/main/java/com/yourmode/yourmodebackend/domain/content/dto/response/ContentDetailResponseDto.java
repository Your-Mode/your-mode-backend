package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentDetailResponseDto {
    private Integer id;
    private String title;
    private String mainImgUrl;
    private boolean isRecommended;
    private LocalDateTime publishAt;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private List<CategoryDto> categories;
    private List<BodyTypeDto> bodyTypes;
    private List<ContentBlockDto> blocks;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;

    @Data
    public static class CategoryDto {
        private Integer id;
        private String name;
    }

    @Data
    public static class ContentBlockDto {
        private Integer blockType;
        private String contentData;
        private Integer blockOrder;
        private ContentBlockStyleDto style;
        private List<ContentBlockImageDto> images;
    }

    @Data
    public static class ContentBlockStyleDto {
        private String fontFamily;
        private Integer fontSize;
        private String fontWeight;
        private String textColor;
        private String backgroundColor;
        private String textAlign;
    }

    @Data
    public static class ContentBlockImageDto {
        private String imageUrl;
        private Integer imageOrder;
    }

    @Data
    public static class BodyTypeDto {
        private Integer id;
        private String name;
    }
} 