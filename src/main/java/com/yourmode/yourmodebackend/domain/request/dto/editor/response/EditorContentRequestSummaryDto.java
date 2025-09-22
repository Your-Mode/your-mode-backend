package com.yourmode.yourmodebackend.domain.request.dto.editor.response;

import com.yourmode.yourmodebackend.domain.request.dto.ContentRequestStatusHistoryDto;
import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EditorContentRequestSummaryDto {
    private final UserProfileSummaryDto profile;
    private final Integer id;
    private final String bodyFeature;
    private final String situation;
    private final String recommendedStyle;

    private final List<Integer> itemCategoryIds;       // 기존 유지 (필요 시)
    private final List<String> itemCategoryNames;

    private final LocalDateTime createdAt;

    private final List<ContentRequestStatusHistoryDto> statusHistories; // 상태 변경 이력
}