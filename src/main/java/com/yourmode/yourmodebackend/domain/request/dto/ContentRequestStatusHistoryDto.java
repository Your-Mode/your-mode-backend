package com.yourmode.yourmodebackend.domain.request.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestStatusHistoryDto {
    private LocalDateTime changedAt;
    private String statusName;
    private Integer editorId;
    private String editorName;
}
