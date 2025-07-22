package com.yourmode.yourmodebackend.domain.content.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentCreateRequestDto {
    private String title;
    private String mainImgUrl;
    private boolean isRecommended;
    private String publishType;
    private LocalDateTime publishAt;
    private List<Integer> categoryIds;
    private List<ContentBlockDto> blocks;

    @Data
    public static class ContentBlockDto {
        private Integer blockType; // 1: image, 2: text, 3: image_group
        private String contentData; // 텍스트 or 기타 데이터
        private Integer blockOrder;
        private ContentBlockStyleDto style;
        private List<ContentBlockImageDto> images; // 이미지 블록일 경우
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
} 