package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentListResponseDto {
    private Integer id;
    private String title;
    private String mainImgUrl;
    private boolean isRecommended;
    private LocalDateTime publishAt;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private List<CategoryDto> categories;
    private List<BodyTypeDto> bodyTypes;

    @Data
    public static class CategoryDto {
        private Integer id;
        private String name;
    }

    @Data
    public static class BodyTypeDto {
        private Integer id;
        private String name;
    }
} 