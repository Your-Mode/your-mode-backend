package com.yourmode.yourmodebackend.global.config.security.auth;

import com.yourmode.yourmodebackend.domain.user.dto.internal.UserWithCredential;
import com.yourmode.yourmodebackend.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PrincipalDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserWithCredential userWithCredential = userMapper.findUserWithCredentialByEmail(email);

        if (userWithCredential == null ||
                userWithCredential.getUser() == null ||
                userWithCredential.getCredential() == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return new PrincipalDetails(
                userWithCredential.getUser(),
                userWithCredential.getCredential().getPasswordHash()
        );
    }
}
