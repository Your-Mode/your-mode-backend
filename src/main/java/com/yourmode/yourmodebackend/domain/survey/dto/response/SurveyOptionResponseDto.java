package com.yourmode.yourmodebackend.domain.survey.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "설문 옵션 응답 DTO")
public class SurveyOptionResponseDto {
    @Schema(description = "옵션 ID", example = "1")
    private Integer optionId;
    @Schema(description = "옵션 내용", example = "피부가 탄탄하고 쫀쫀한 탄력감이 느껴진다.")
    private String optionContent;
} 