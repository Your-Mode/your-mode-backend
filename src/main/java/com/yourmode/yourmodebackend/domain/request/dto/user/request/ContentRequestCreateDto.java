package com.yourmode.yourmodebackend.domain.request.dto.user.request;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "컨텐츠 요청 생성 DTO")
public class ContentRequestCreateDto {

    @Schema(description = "체형 특징", example = "어깨가 좁은 체형")
    private String bodyFeature;

    @Schema(description = "상황 설명", example = "결혼식 참석 예정")
    private String situation;

    @Schema(description = "추천 스타일", example = "캐주얼, 심플")
    private String recommendedStyle;

    @Schema(description = "피해야 할 스타일", example = "화려한 패턴, 과한 액세서리")
    private String avoidedStyle;

    @Schema(description = "예산 (단위: 만원)", example = "50")
    private Integer budget;

    @Schema(description = "공개 여부", example = "true")
    private Boolean isPublic;

    @Schema(
            description = "선택한 아이템 카테고리 ID 리스트\n" +
                    "1=아우터, 2=상의, 3=하의, 4=가방, 5=신발, 6=악세서리, 7=기타",
            example = "[1, 2, 3]"
    )
    private List<Long> itemCategoryIds;
}
