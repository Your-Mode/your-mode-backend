package com.yourmode.yourmodebackend.domain.request.dto.editor.response;

import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
import lombok.Getter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EditorContentRequestDetailDto {
    private final Integer id;
    private final UserProfileSummaryDto profile;
    private final String bodyFeature;
    private final String situation;
    private final String recommendedStyle;
    private final String avoidedStyle;
    private final Integer budget;
    private final Boolean isPublic;
    private final String status;

    private final List<Integer> itemCategoryIds;       // 기존 유지 (필요 시)
    private final List<String> itemCategoryNames;

    private final LocalDateTime createdAt;
}
