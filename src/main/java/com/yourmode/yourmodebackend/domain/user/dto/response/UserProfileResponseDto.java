package com.yourmode.yourmodebackend.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import com.yourmode.yourmodebackend.domain.user.enums.Gender;

@Getter
@Builder
@AllArgsConstructor
public class UserProfileResponseDto {
    private String email;
    private String name;
    private String phoneNumber;
    private Float height;
    private Float weight;
    private Integer bodyTypeId;
    private Gender gender;
}
