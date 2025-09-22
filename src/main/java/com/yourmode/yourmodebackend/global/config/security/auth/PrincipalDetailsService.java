package com.yourmode.yourmodebackend.global.config.security.auth;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserCredential;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 사용자 인증 요청: email={}", email);

        User user = userRepository.findByEmailWithCredential(email)  // 변경된 부분
                .orElseThrow(() -> {
                    log.warn("❌ 사용자 없음: email={}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.info("✅ 사용자 정보 조회 성공: id={}, name={}", user.getId(), user.getName());

        UserCredential credential = user.getCredential();

        if (credential == null) {
            log.error("❌ 사용자 자격 증명 없음: email={}", email);
            throw new UsernameNotFoundException("Credentials not found for user with email: " + email);
        }

        log.info("🔐 자격 증명 정보 존재: passwordHash={}", credential.getPasswordHash());

        return new PrincipalDetails(user, credential.getPasswordHash());
    }
}

