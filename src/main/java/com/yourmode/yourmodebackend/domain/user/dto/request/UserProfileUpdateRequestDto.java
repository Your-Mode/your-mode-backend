package com.yourmode.yourmodebackend.domain.user.dto.request;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유저 프로필 수정 요청 DTO")
public class UserProfileUpdateRequestDto {

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "유효한 휴대폰 번호를 입력해주세요.")
    private String phoneNumber;

    @Schema(description = "사용자 키 (cm 단위)", example = "175.5")
    @NotNull(message = "키를 입력해주세요.")
    @DecimalMin(value = "50.0", message = "키는 50cm 이상이어야 합니다.")
    @DecimalMax(value = "300.0", message = "키는 300cm 이하이어야 합니다.")
    private Float height;

    @Schema(description = "사용자 몸무게 (kg 단위)", example = "68.2")
    @NotNull(message = "몸무게를 입력해주세요.")
    @DecimalMin(value = "10.0", message = "몸무게는 10kg 이상이어야 합니다.")
    @DecimalMax(value = "300.0", message = "몸무게는 300kg 이하이어야 합니다.")
    private Float weight;

    @Schema(description = "성별 (MALE 또는 FEMALE)", example = "MALE")
    @NotNull(message = "성별을 선택해주세요.")
    private Gender gender;

    @Schema(description = "체형 ID (1: 스트레이트, 2: 웨이브, 3: 내추럴, 4: 선택안함)", example = "1")
    @NotNull(message = "체형을 선택해주세요.")
    private Long bodyTypeId;
}
