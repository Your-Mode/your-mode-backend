package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import lombok.Data;

@Data
public class UserProfile {
    private Long profileId;
    private Float height;
    private Float weight;
    private Gender gender;

    private Long bodyTypeId; // FK → body_types
    private Long userId;     // FK → users
}
