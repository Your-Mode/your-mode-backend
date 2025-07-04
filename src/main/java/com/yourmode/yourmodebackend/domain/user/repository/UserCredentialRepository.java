package com.yourmode.yourmodebackend.domain.user.repository;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserCredential;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByOauthIdAndOauthProvider(String oauthId, OAuthProvider provider);
    Optional<UserCredential> findByUserEmail(String email); // 연관관계 기반
    Optional<UserCredential> findByUser(User user);
}
