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
    private UserProfileSummaryDto profile;
    private Long id;
    private String bodyFeature;
    private String situation;
    private String recommendedStyle;

    private List<Long> itemCategoryIds;       // 기존 유지 (필요 시)
    private List<String> itemCategoryNames;

    private LocalDateTime createdAt;

    private List<ContentRequestStatusHistoryDto> statusHistories; // 상태 변경 이력
}