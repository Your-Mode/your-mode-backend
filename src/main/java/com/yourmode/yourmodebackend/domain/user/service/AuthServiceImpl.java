package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.domain.User;
import com.yourmode.yourmodebackend.domain.user.domain.UserCredential;
import com.yourmode.yourmodebackend.domain.user.domain.UserProfile;
import com.yourmode.yourmodebackend.domain.user.domain.UserToken;
import com.yourmode.yourmodebackend.domain.user.dto.internal.UserWithProfile;
import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.domain.user.mapper.UserMapper;

import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.security.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

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
     * @throws RestApiException
     *         - 이메일 중복 시 DUPLICATE_EMAIL 상태로 예외 발생
     *         - DB 저장 중 오류 발생 시 DB_INSERT_FAILED 상태로 예외 발생
     *         - 인증 실패 시 AUTHENTICATION_FAILED 상태로 예외 발생
     */
    @Transactional
    public AuthResponseDto signUp(LocalSignupRequestDto request) {
        // 이메일 중복 체크 후 중복되면 예외 발생
        validateDuplicateEmail(request.getEmail());

        // User 생성 및 저장, DB에서 자동 생성된 PK(userId) 필드 값이 세팅됨
        User user = createAndSaveUser(request);

        // UserCredential 저장: 비밀번호 해시값 + OAuthProvider 정보
        saveUserCredential(user.getUserId(), request.getPassword(), OAuthProvider.LOCAL, null);

        // UserProfile 저장: 키, 몸무게, 성별, 체형 등 프로필 정보
        saveUserProfile(user.getUserId(), request);

        try {
            // AuthenticationManager를 통한 로그인 처리
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 인증된 사용자 정보 획득
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Long userId = principalDetails.getUserId();
            String email = principalDetails.getEmail();

            // JWT 액세스 토큰 및 리프레시 토큰 생성
            JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
            JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

            saveUserToken(userId, refresh.token(), refresh.expiry());

            // 응답용 유저 정보 DTO 생성 (이름, 역할, 체형ID 포함)
            UserInfoDto userInfo = UserInfoDto.builder()
                    .name(principalDetails.getName())
                    .role(principalDetails.getRole())
                    .bodyTypeId(request.getBodyTypeId())
                    .build();

            // 액세스 토큰, 리프레시 토큰, 유저 정보 포함한 응답 DTO 반환
            return AuthResponseDto.builder()
                    .accessToken(access.token())
                    .refreshToken(refresh.token())
                    .user(userInfo)
                    .build();

        } catch (AuthenticationException ex) {
            throw new RestApiException(UserErrorStatus.AUTHENTICATION_FAILED);
        }
    }

    /**
     * 이메일 중복 검증
     *
     * @param email 검사할 이메일
     * @throws RestApiException 중복된 이메일이 존재 시
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
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private User createAndSaveUser(CommonSignupRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.USER);
        user.setIsTermsAgreed(request.getIsTermsAgreed());
        user.setIsPrivacyPolicyAgreed(request.getIsPrivacyPolicyAgreed());
        user.setIsMarketingAgreed(request.getIsMarketingAgreed());
        user.setCreatedAt(LocalDateTime.now());

        try {
            // insert 후 user.userId 필드에 DB에서 생성된 PK 값이 세팅됨
            userMapper.insertUser(user);
        } catch (DuplicateKeyException e) {
            if (e.getMessage().contains("phone_number")) {
                throw new RestApiException(UserErrorStatus.DUPLICATE_PHONE_NUMBER);
            }
        } catch (DataAccessException e) {
            // 그 외 DB 관련 예외
            throw new RestApiException(UserErrorStatus.DB_INSERT_FAILED);
        }

        return user;
    }

    /**
     * UserCredential 저장
     * 비밀번호는 암호화 후 저장하며 OAuthProvider 설정
     * OAuthID는 고려하지 않음 -> 추후 고려 여부 결정
     *
     * @param userId     대상 유저 PK
     * @param rawPassword 평문 비밀번호 (암호화되어 저장됨)
     * @param provider   로그인 제공자 정보 (LOCAL, KAKAO 등)
     * @param oauthId    OAuth 로그인 사용자의 고유 ID (소셜 로그인인 경우), 일반 로그인은 null
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserCredential(Long userId, String rawPassword, OAuthProvider provider, String oauthId) {
        UserCredential credential = new UserCredential();
        credential.setUserId(userId);
        if (rawPassword != null) {
            credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else {
            credential.setPasswordHash(null);
        }
        credential.setOauthProvider(provider);
        credential.setOauthId(oauthId); // null for local, Kakao

        try {
            userMapper.insertUserCredential(credential);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_CREDENTIAL_INSERT_FAILED);
        }

    }

    /**
     * UserProfile 저장
     * 키, 몸무게, 성별, 체형 ID 등 프로필 정보 저장
     *
     * @param userId  대상 유저 PK
     * @param request 회원가입 요청 정보 (로컬/소셜 가입 모두 지원, CommonSignupRequest 구현체)
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserProfile(Long userId, CommonSignupRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBodyTypeId(request.getBodyTypeId());

        try {
            userMapper.insertUserProfile(profile);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_PROFILE_INSERT_FAILED);
        }
    }

    /**
     * Refresh Token 저장
     * DB에 refresh token과 만료 시간을 기록
     *
     * @param userId 대상 유저 PK
     * @param refreshToken 발급된 리프레시 토큰 문자열
     * @param expiredAt 토큰 만료일시
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserToken(Long userId, String refreshToken, LocalDateTime expiredAt) {
        UserToken userToken = new UserToken();
        userToken.setUserId(userId);
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiredAt(expiredAt);

        try {
            userMapper.insertUserToken(userToken);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_TOKEN_INSERT_FAILED);
        }
    }

    /**
     * 로컬 로그인 처리
     * 1) 이메일과 비밀번호로 인증 토큰 생성
     * 2) AuthenticationManager로 인증 시도 (비밀번호 검증 포함)
     * 3) JWT 액세스 토큰과 리프레시 토큰 생성 및 저장
     * 4) 응답용 DTO 생성 및 반환
     *
     * @param request 로그인 요청 DTO (이메일, 비밀번호)
     * @return JWT 토큰과 유저 정보가 포함된 응답 DTO
     * @throws RestApiException 인증 실패 시 AUTHENTICATION_FAILED 상태로 예외 발생
     */
    @Transactional
    public AuthResponseDto login(LocalLoginRequestDto request) {
        try {
            // 인증 토큰 생성: 이메일, 비밀번호 정보 포함
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            // authenticationManager를 통해 인증 시도 (UserDetailsService + PasswordEncoder가 검증 수행)
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 인증 성공 시 인증 객체에서 사용자 정보 획득
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Long userId = principalDetails.getUserId();
            String email = principalDetails.getEmail();

            // JWT 액세스 토큰 및 리프레시 토큰 생성
            JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
            JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

            // 발급된 리프레시 토큰 DB 저장
            saveUserToken(userId, refresh.token(), refresh.expiry());

            // 응답용 유저 정보 DTO 생성 (이름 포함)
            UserInfoDto userInfo = UserInfoDto.builder()
                    .name(principalDetails.getName())
                    .build();

            // 액세스 토큰, 리프레시 토큰, 유저 정보 포함 응답 DTO 반환
            return AuthResponseDto.builder()
                    .accessToken(access.token())
                    .refreshToken(refresh.token())
                    .user(userInfo)
                    .build();

        } catch (BadCredentialsException e) {
            throw new RestApiException(UserErrorStatus.INVALID_CREDENTIALS);
        } catch (AuthenticationException e) {
            throw new RestApiException(UserErrorStatus.AUTHENTICATION_FAILED);
        }
    }

    /**
     * 인가 코드를 이용해 카카오에서 액세스 토큰과 리프레시 토큰을 요청하는 메서드
     *
     * @param authorizationCode 카카오에서 받은 인가 코드
     * @return 토큰 정보가 담긴 Map (access_token, refresh_token 등)
     * @throws RestApiException 카카오 API 호출 실패  시 발생
     */
    public Map<String, Object> requestTokenWithKakao(String authorizationCode) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", clientId);
            params.add("redirect_uri", redirectUri);
            params.add("code", authorizationCode);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();

        } catch (ResourceAccessException e) {
            // 카카오 서버와 통신 불가
            throw new RestApiException(UserErrorStatus.KAKAO_API_UNAVAILABLE);
        } catch (Exception e) {
            throw new RestApiException(UserErrorStatus.KAKAO_TOKEN_REQUEST_FAILED);
        }
    }


    /**
     * 액세스 토큰을 이용해 카카오 사용자 정보를 요청하는 메서드
     *
     * @param accessToken 카카오에서 발급받은 액세스 토큰
     * @return 사용자 정보가 담긴 Map (이메일, 프로필 등)
     */
    public Map<String, Object> requestUserInfoWithKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RestApiException(UserErrorStatus.KAKAO_USERINFO_REQUEST_FAILED);
        }
    }

    /**
     * 카카오 인가 코드로 로그인 처리하는 메서드
     * 1) 인가 코드로 토큰 발급
     * 2) 토큰으로 사용자 정보 조회
     * 3) 이메일로 회원가입 여부 확인
     * 4) 신규회원이면 사용자 정보를 반환 -> 추가 정보 입력, 기존 회원이면 JWT 발급하여 로그인 처리
     *
     * @param request authorizationCode 카카오 인가 코드
     * @return 로그인 응답 DTO (JWT 토큰 등)
     * @throws RestApiException 토큰 발급 실패, 이메일 누락, 회원 미존재 등 예외 상황
     */
    @Transactional
    public AuthResponseDto processKakaoLogin(KakaoLoginRequestDto request) {
        // 카카오 토큰 발급
        Map<String, Object> tokenInfo = requestTokenWithKakao(request.getAuthorizationCode());
        String accessToken = (String) tokenInfo.get("access_token");

        // 카카오 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = requestUserInfoWithKakao(accessToken);
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoUserInfo.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // 회원 존재 여부 확인
        if (!userMapper.isEmailExists(email)) {
            // 신규 회원: 추가 정보 입력이 필요함을 응답
            KakaoSignupRequestDto kakaoSignupRequest = KakaoSignupRequestDto.builder()
                    .email(email)
                    .name(nickname)
                    .build();

            return AuthResponseDto.ofNeedAdditionalInfo(kakaoSignupRequest);
        }

        // 기존 회원 → DB에서 유저 조회
        UserWithProfile userWithProfile = userMapper.findUserWithProfileByEmail(email);
        if (userWithProfile == null || userWithProfile.getUser() == null) {
            throw new RestApiException(UserErrorStatus.USER_NOT_FOUND);
        }

        Long userId = userWithProfile.getUser().getUserId();

        PrincipalDetails principalDetails = new PrincipalDetails(userWithProfile.getUser(), "");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 발급
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

        saveUserToken(userId, refresh.token(), refresh.expiry());

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(userWithProfile.getUser().getName())
                .role(userWithProfile.getUser().getRole())
                .bodyTypeId(userWithProfile.getProfile().getBodyTypeId())
                .build();

        return AuthResponseDto.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .user(userInfo)
                .build();
    }

    /**
     * 카카오 회원가입 완료 처리 메서드
     * 1) 이메일 중복 여부 확인
     * 2) User 엔티티 생성 및 저장
     * 3) UserCredential 저장 (비밀번호는 null, Kakao OAuth 정보 포함)
     * 4) UserProfile 저장 (키, 몸무게, 성별, 체형 등 프로필 정보)
     * 5) JWT 토큰 생성 및 저장
     * 6) 로그인 완료 응답 반환
     *
     * @param request 카카오 회원가입 요청 DTO (추가 정보 포함)
     * @return 로그인 응답 DTO (JWT 토큰, 사용자 정보 등)
     */
    @Transactional
    public AuthResponseDto completeSignupWithKakao(KakaoSignupRequestDto request) {
        // 이메일 중복 체크 및 회원 생성, 저장
        validateDuplicateEmail(request.getEmail());
        User user = createAndSaveUser(request);
        saveUserCredential(user.getUserId(), null, OAuthProvider.KAKAO, null);
        saveUserProfile(user.getUserId(), request);

        // PrincipalDetails 생성 (password는 null or "")
        PrincipalDetails principalDetails = new PrincipalDetails(user, "");

        // 인증 토큰 생성 (인증매니저가 처리할 authentication)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

        // 인증 성공 시 SecurityContext에 저장 (로그인 상태 유지)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성 및 저장
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(user.getUserId(), user.getEmail());
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(user.getUserId(), user.getEmail());
        saveUserToken(user.getUserId(), refresh.token(), refresh.expiry());

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(user.getName())
                .role(user.getRole())
                .bodyTypeId(request.getBodyTypeId())
                .build();

        return AuthResponseDto.builder()
                .accessToken(access.token())
                .refreshToken(refresh.token())
                .user(userInfo)
                .build();
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 재발급하는 메서드
     *
     * @param request RefreshTokenRequestDto - 클라이언트로부터 전달받은 리프레시 토큰
     * @return AuthResponseDto 새로 발급된 토큰과 사용자 정보가 포함된 응답 DTO
     * @throws RestApiException 토큰 유효성 실패 혹은 사용자 정보 미발견 시 예외 발생
     */
    @Transactional
    public AuthResponseDto refreshAccessToken(RefreshTokenRequestDto request) {
        String refreshToken = request.getRefreshToken();

        // 리프레시 토큰 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            // 만료 검사 구분 예시
            if (jwtProvider.isTokenExpired(refreshToken)) {
                throw new RestApiException(UserErrorStatus.EXPIRED_REFRESH_TOKEN);
            }
            throw new RestApiException(UserErrorStatus.INVALID_TOKEN);
        }

        // 리프레시 토큰에서 사용자 정보 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        // DB에서 리프레시 토큰 확인
        UserToken savedToken = userMapper.findUserTokensByUserId(userId);
        if (savedToken == null || !savedToken.getRefreshToken().equals(refreshToken)) {
            throw new RestApiException(UserErrorStatus.INVALID_TOKEN);
        }

        // 새 토큰 발급
        JwtProvider.JwtWithExpiry newAccess = jwtProvider.generateAccessToken(userId, email);
        JwtProvider.JwtWithExpiry newRefresh = jwtProvider.generateRefreshToken(userId, email);

        // DB에 리프레시 토큰 업데이트
        userMapper.updateRefreshToken(userId, newRefresh.token(), newRefresh.expiry());

        // 사용자 정보 조회
        UserWithProfile user = userMapper.findUserWithProfileByEmail(email);
        if (user == null) throw new RestApiException(UserErrorStatus.USER_NOT_FOUND);

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(user.getUser().getName())
                .role(user.getUser().getRole())
                .bodyTypeId(user.getProfile().getBodyTypeId())
                .build();

        return AuthResponseDto.builder()
                .accessToken(newAccess.token())
                .refreshToken(newRefresh.token())
                .user(userInfo)
                .build();
    }

    /**
     * 로그아웃 처리 메서드.
     *
     * 현재 로그인한 사용자의 리프레시 토큰을 데이터베이스에서 삭제하여 로그아웃을 수행
     *
     * @param principal 현재 인증된 사용자의 인증 정보(PrincipalDetails)
     * @return UserIdResponseDto 삭제 대상 사용자 ID를 포함한 응답 DTO
     * @throws RestApiException 삭제할 리프레시 토큰이 존재하지 않을 경우 또는 삭제 과정에서 예외가 발생할 경우
     *         - LOGOUT_NO_ACTIVE_SESSION: 삭제할 토큰이 없어 활성 세션이 없다고 판단할 때 발생
     *         - LOGOUT_FAILED: 토큰 삭제 과정에서 예기치 못한 예외가 발생했을 때 발생
     */
    public UserIdResponseDto logout(PrincipalDetails principal) {
        Long userId = principal.getUserId();
        try {
            int deletedCount = userMapper.deleteUserToken(userId);
            if (deletedCount == 0) {
                log.warn("No refresh token found for deletion. userId={}", userId);
                throw new RestApiException(UserErrorStatus.LOGOUT_NO_ACTIVE_SESSION);
            }
            return new UserIdResponseDto(userId);
        } catch (Exception e) {
            log.error("Error occurred while deleting refresh token. userId={}", userId, e);
            throw new RestApiException(UserErrorStatus.LOGOUT_FAILED); // 적절한 에러 코드 정의
        }
    }

}
