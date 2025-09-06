package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentViewResponseDto {
    private Integer userId;
    private Integer contentId;
    private LocalDateTime viewedAt;
    private String message;
}
