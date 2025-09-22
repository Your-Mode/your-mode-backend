package com.yourmode.yourmodebackend.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageComponentResponseDto {
    private String email;
    private Integer bodyTypeId;
    private Long customContentsCount;
    private Long viewedContentsCount;
    private Long likedContentsCount;
    private Long commentedContentsCount;
    private Long myCommentsCount;
}
