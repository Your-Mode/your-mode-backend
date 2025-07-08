package com.yourmode.yourmodebackend.domain.request.dto.user.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestSummaryDto {

    private Integer id;
    private String bodyFeature;
    private String situation;
    private Integer budget;
    private Boolean isPublic;
    private LocalDateTime createdAt;

    private List<ContentRequestStatusHistoryDto> statusHistories; // 상태 변경 이력
}