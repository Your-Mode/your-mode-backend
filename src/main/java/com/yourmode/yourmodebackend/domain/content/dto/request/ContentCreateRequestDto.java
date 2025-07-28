package com.yourmode.yourmodebackend.domain.content.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentCreateRequestDto {
    private String title;
    /**
     * S3 presigned URL로 업로드된 이미지의 S3 URL을 넣어야 함
     */
    private String mainImgUrl;
    private boolean isRecommended;
    private LocalDateTime publishAt;
    private Integer contentsRequestId;
    private List<Integer> categoryIds;
    private List<Integer> bodyTypeIds;
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
        /**
         * S3 presigned URL로 업로드된 이미지의 S3 URL을 넣어야 함
         */
        private String imageUrl;
        private Integer imageOrder;
    }
} 