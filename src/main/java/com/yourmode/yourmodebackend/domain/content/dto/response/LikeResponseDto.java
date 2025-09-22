package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LikeResponseDto {
    private Integer id;
    private LocalDateTime createdAt;
    private Integer userId;
    private String userName;
    private Integer contentId;
}
