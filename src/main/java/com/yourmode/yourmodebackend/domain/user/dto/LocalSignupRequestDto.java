package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import lombok.Data;

@Data
public class LocalSignupRequestDto {
    // User 정보
    private String email;
    private String password; // UserCredential.passwordHash
    private String name;
    private String phoneNumber;
    private Boolean isMarketingAgreed;

    // UserProfile 정보
    private Float height;
    private Float weight;
    private Gender gender;
    private Long bodyTypeId;
}
