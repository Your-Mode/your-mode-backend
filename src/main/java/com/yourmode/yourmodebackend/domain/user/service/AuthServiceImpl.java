package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserCredential;
import com.yourmode.yourmodebackend.domain.user.domain.UserProfile;
import com.yourmode.yourmodebackend.domain.user.domain.UserToken;
import com.yourmode.yourmodebackend.domain.user.dto.*;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.domain.user.mapper.UserMapper;

import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.jwt.CustomUserDetails;
import com.yourmode.yourmodebackend.global.config.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 로컬 회원가입 처리
     * 1) 이메일 중복 검사
     * 2) User 엔티티 생성 및 DB 저장 (userId 자동 생성)
     * 3) UserCredential 저장 (암호화된 비밀번호, OAuthProvider 설정)
     * 4) UserProfile 저장 (신체 정보 등)
     * 5) Spring Security 인증 매니저로 로그인 처리 (비밀번호 검증 수행)
     * 6) JWT 액세스 토큰, 리프레시 토큰 발급 및 DB 저장
     * 7) 응답용 DTO 구성 및 반환
     *
     * @param request 로컬 회원가입 요청 DTO
     * @return JWT 토큰과 유저 정보가 포함된 응답 DTO
     * @throws RestApiException 이메일 중복 시 예외 발생
     */
    @Transactional
    public AuthResponseDto signUp(LocalSignupRequestDto request) {
        // 이메일 중복 체크 후 중복되면 예외 발생
        validateDuplicateEmail(request.getEmail());

        // User 생성 및 저장, DB에서 자동 생성된 PK(userId) 필드 값이 세팅됨
        User user = createAndSaveUser(request);

        // UserCredential 저장: 비밀번호 해시값 + OAuthProvider 정보
        saveUserCredential(user.getUserId(), request.getPassword());

        // UserProfile 저장: 키, 몸무게, 성별, 체형 등 프로필 정보
        saveUserProfile(user.getUserId(), request);

        // AuthenticationManager를 통한 로그인 처리
        // UserDetailsService, PasswordEncoder가 내부적으로 비밀번호 검증 수행
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 인증된 사용자 정보 획득
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        // JWT 액세스 토큰 및 리프레시 토큰 생성
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId);
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId);

        saveUserToken(userId, refresh.token(), refresh.expiry());

        // 응답용 유저 정보 DTO 생성 (이름, 역할, 체형ID 포함)
        UserInfoDto userInfo = UserInfoDto.builder()
                .name(userDetails.getName())
                .role(userDetails.getRole())
                .bodyTypeId(request.getBodyTypeId())
                .build();

        // 액세스 토큰, 리프레시 토큰, 유저 정보 포함한 응답 DTO 반환
        return AuthResponseDto.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .user(userInfo)
                .build();
    }

    /**
     * 이메일 중복 검증
     * 중복된 이메일이 존재하면 커스텀 예외(RestApiException)를 던짐
     *
     * @param email 검사할 이메일
     */
    private void validateDuplicateEmail(String email) {
        if (userMapper.isEmailExists(email)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
        }
    }

    /**
     * User 엔티티 생성 및 DB 저장
     * 자동 생성된 PK(userId)가 객체에 세팅됨
     *
     * @param request 회원가입 요청 DTO
     * @return 저장된 User 엔티티 (userId 포함)
     */
    private User createAndSaveUser(LocalSignupRequestDto request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setIsTermsAgreed(request.getIsTermsAgreed());
        user.setIsPrivacyPolicyAgreed(request.getIsPrivacyPolicyAgreed());
        user.setIsMarketingAgreed(request.getIsMarketingAgreed());
        user.setCreatedAt(LocalDateTime.now());

        // insert 후 user.userId 필드에 DB에서 생성된 PK 값이 세팅됨
        userMapper.insertUser(user);
        return user;
    }

    /**
     * UserCredential 저장
     * 비밀번호는 암호화 후 저장하며 OAuthProvider 지정
     *
     * @param userId 대상 유저 PK
     * @param rawPassword 평문 비밀번호
     */
    private void saveUserCredential(Long userId, String rawPassword) {
        UserCredential credential = new UserCredential();
        credential.setUserId(userId);
        credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        credential.setOauthProvider(OAuthProvider.LOCAL);
        credential.setOauthId(null);

        userMapper.insertUserCredential(credential);
    }

    /**
     * UserProfile 저장
     * 키, 몸무게, 성별, 체형 ID 등 프로필 정보 저장
     *
     * @param userId 대상 유저 PK
     * @param request 회원가입 요청 DTO (프로필 정보 포함)
     */
    private void saveUserProfile(Long userId, LocalSignupRequestDto request) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBodyTypeId(request.getBodyTypeId());

        userMapper.insertUserProfile(profile);
    }

    /**
     * Refresh Token 저장
     * DB에 refresh token과 만료 시간을 기록
     *
     * @param userId 대상 유저 PK
     * @param refreshToken 발급된 리프레시 토큰 문자열
     * @param expiredAt 토큰 만료일시
     */
    private void saveUserToken(Long userId, String refreshToken, LocalDateTime expiredAt) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiredAt(expiredAt);

        userMapper.insertUserToken(userToken);
    }

    /**
     * 로컬 로그인 처리
     * 1) 이메일과 비밀번호로 인증 토큰 생성
     * 2) AuthenticationManager로 인증 시도 (비밀번호 검증 포함)
     * 3) 인증 성공 시 CustomUserDetails에서 유저 정보 획득
     * 4) JWT 액세스 토큰과 리프레시 토큰 생성 및 저장
     * 5) 응답용 DTO 생성 및 반환
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return JWT 토큰 및 유저 정보 포함한 응답 DTO
     * @throws org.springframework.security.core.AuthenticationException 인증 실패 시 예외 발생
     */
    public AuthResponseDto login(LocalLoginRequestDto request) {

        // 인증 토큰 생성: 이메일, 비밀번호 정보 포함
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 인증 매니저를 통해 인증 시도 (UserDetailsService + PasswordEncoder가 검증 수행)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 인증 성공 시 인증 객체에서 사용자 정보 획득
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getUserId();

        // JWT 액세스 토큰 및 리프레시 토큰 생성
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId);
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId);

        // 발급된 리프레시 토큰 DB 저장
        saveUserToken(userId, refresh.token(), refresh.expiry());

        // 응답용 유저 정보 DTO 생성 (이름 포함)
        UserInfoDto userInfo = UserInfoDto.builder()
                .name(userDetails.getName())
                .build();

        // 액세스 토큰, 리프레시 토큰, 유저 정보 포함 응답 DTO 반환
        return AuthResponseDto.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .user(userInfo)
                .build();
    }

    // todo: 소셜 로그인, 로그아웃, 토큰 재발급, 비밀번호 변경 등 기능 구현
}
