package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserProfile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWithProfile {
    private User user;
    private UserProfile profile;
}
