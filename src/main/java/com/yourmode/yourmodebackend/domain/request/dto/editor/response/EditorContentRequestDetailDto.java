package com.yourmode.yourmodebackend.domain.request.dto.editor.response;

import com.yourmode.yourmodebackend.domain.request.dto.UserProfileSummaryDto;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class EditorContentRequestDetailDto {
    private Long id;
    private User user;
    private UserProfileSummaryDto profile;
    private String bodyFeature;
    private String situation;
    private String recommendedStyle;
    private String avoidedStyle;
    private Integer budget;
    private Boolean isPublic;
    private String status;

    private List<Long> itemCategoryIds;       // 기존 유지 (필요 시)
    private List<String> itemCategoryNames;

    private LocalDateTime createdAt;
}
