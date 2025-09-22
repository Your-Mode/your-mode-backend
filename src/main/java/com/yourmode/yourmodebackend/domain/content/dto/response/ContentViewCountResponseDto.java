package com.yourmode.yourmodebackend.domain.content.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentViewCountResponseDto {
    private Integer contentId;
    private Long viewCount;
}
