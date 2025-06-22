package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import lombok.Data;

@Data
public class UserInfoDto {
    private String email;
    private String name;
    private String phoneNumber;
    private Boolean isMarketingAgreed;
    private UserRole role;
    private Float height;
    private Float weight;
    private Gender gender;
    private Long bodyTypeId;
}
