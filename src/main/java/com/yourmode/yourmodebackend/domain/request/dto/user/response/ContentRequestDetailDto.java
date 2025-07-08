package com.yourmode.yourmodebackend.domain.request.dto.user.response;

import com.yourmode.yourmodebackend.domain.user.entity.UserProfile;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestDetailDto {

    private UserProfile profile;
    private Integer id;
    private String bodyFeature;
    private String situation;
    private String recommendedStyle;
    private String avoidedStyle;
    private Integer budget;
    private Boolean isPublic;
    private List<Integer> itemCategoryIds;
    private List<ContentRequestStatusHistoryDto> statusHistories; // 상태 변경 이력
}