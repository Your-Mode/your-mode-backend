package com.yourmode.yourmodebackend.domain.user.domain;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private Long profileId;
    private Float height;
    private Float weight;
    private Gender gender;

    private Long bodyTypeId; // FK → body_types
    private Long userId;     // FK → users
}
