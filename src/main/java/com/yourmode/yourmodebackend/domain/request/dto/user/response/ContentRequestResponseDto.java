package com.yourmode.yourmodebackend.domain.request.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ContentRequestResponseDto {
    private Long id;
    private String situation;
    private String bodyFeature;
    private String recommendedStyle;
    private String avoidedStyle;
    private Integer budget;
    private Boolean isPublic;
    private String status;
    private List<String> itemCategoryNames;
    private LocalDateTime createdAt;
}

