package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserCredential;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWithCredential {
    private User user;
    private UserCredential credential;
}