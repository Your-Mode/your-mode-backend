package com.yourmode.yourmodebackend.domain.request.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestStatusHistoryDto {
    private LocalDateTime changedAt;
    private String statusName;
    private Long editorId;
    private String editorName;
}
