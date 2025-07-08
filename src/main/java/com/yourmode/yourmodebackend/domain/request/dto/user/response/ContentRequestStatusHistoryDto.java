package com.yourmode.yourmodebackend.domain.request.dto.user.response;

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
    private Integer editorId;
}
