package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String name;
    private UserRole role;
    private Long bodyTypeId;
}
