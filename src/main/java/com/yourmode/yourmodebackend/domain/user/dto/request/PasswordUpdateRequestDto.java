package com.yourmode.yourmodebackend.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordUpdateRequestDto {
    @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함 8자 이상)", example = "P@ssw0rd!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])[a-zA-Z\\d!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]{8,}$",
        message = "비밀번호는 8자 이상, 영문/숫자/특수문자를 포함해야 합니다."
    )
    private String newPassword;
}