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
    private final Long id;
    private final String situation;
    private final String bodyFeature;
    private final String recommendedStyle;
    private final String avoidedStyle;
    private final Integer budget;
    private final Boolean isPublic;
    private final String status;
    private final List<String> itemCategoryNames;
    private final LocalDateTime createdAt;
}

