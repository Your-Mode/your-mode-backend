package com.yourmode.yourmodebackend.domain.request.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileSummaryDto {
    private String name;
    private Float height;
    private Float weight;
    private String bodyTypeName;  // bodyType 테이블에서 조회한 이름
}
