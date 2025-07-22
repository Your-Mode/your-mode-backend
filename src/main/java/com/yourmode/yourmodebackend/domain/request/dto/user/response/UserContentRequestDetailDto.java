package com.yourmode.yourmodebackend.domain.request.dto.user.response;

import com.yourmode.yourmodebackend.domain.request.dto.ContentRequestStatusHistoryDto;
import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContentRequestDetailDto {

    private UserProfileSummaryDto profile;
    private Integer id;
    private String bodyFeature;
    private String situation;
    private String recommendedStyle;
    private String avoidedStyle;
    private Integer budget;
    private Boolean isPublic;

    private List<Integer> itemCategoryIds;       // 기존 유지 (필요 시)
    private List<String> itemCategoryNames;

    private List<ContentRequestStatusHistoryDto> statusHistories; // 상태 변경 이력
}