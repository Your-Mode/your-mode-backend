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
@Schema(description = "로컬 회원가입 요청 DTO")
public class LocalSignupRequestDto implements CommonSignupRequest{

    // User 정보
    @Schema(description = "이메일", example = "test@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)", example = "P@ssw0rd!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @Schema(description = "전화번호", example = "01012345678")
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$")
    private String phoneNumber;

    @NotNull(message = "서비스 이용약관 동의는 필수입니다.")
    private Boolean isTermsAgreed;

    @NotNull(message = "개인정보 처리방침 동의는 필수입니다.")
    private Boolean isPrivacyPolicyAgreed;

    private Boolean isMarketingAgreed = false; // 선택 항목

    // UserProfile 정보
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
