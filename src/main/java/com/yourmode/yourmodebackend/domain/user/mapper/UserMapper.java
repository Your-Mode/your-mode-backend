package com.yourmode.yourmodebackend.domain.user.mapper;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserCredential;
import com.yourmode.yourmodebackend.domain.user.domain.UserProfile;
import com.yourmode.yourmodebackend.domain.user.domain.UserToken;
import com.yourmode.yourmodebackend.domain.user.dto.UserWithCredential;
import com.yourmode.yourmodebackend.domain.user.dto.UserWithProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    boolean isEmailExists(@Param("email") String email);
    void insertUser(User user);
    void insertUserProfile(UserProfile profile);
    void insertUserCredential(UserCredential credential);
    void insertUserToken(UserToken token);

    UserWithCredential findUserWithCredentialByEmail(@Param("email") String email);
    UserWithProfile findUserWithProfileByEmail(@Param("email") String email);
}
