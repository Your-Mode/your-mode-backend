package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.entity.*;
import com.yourmode.yourmodebackend.domain.user.dto.request.*;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;

import com.yourmode.yourmodebackend.domain.user.repository.*;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.security.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserTokenRepository userTokenRepository;
    private final BodyTypeRepository bodyTypeRepository;

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
    public AuthResult signUp(LocalSignupRequestDto request) {
        // 이메일 중복 체크 후 중복되면 예외 발생
        validateDuplicateEmail(request.getEmail());

        // User 생성 및 저장, DB에서 자동 생성된 PK(userId) 필드 값이 세팅됨
        User user = createAndSaveUser(request);

        // UserCredential 저장: 비밀번호 해시값 + OAuthProvider 정보
        saveUserCredential(user, request.getPassword(), OAuthProvider.LOCAL, null);

        // UserProfile 저장: 키, 몸무게, 성별, 체형 등 프로필 정보
        saveUserProfile(user, request);

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Integer userId = principalDetails.getUserId();
            String email = principalDetails.getEmail();

            // JWT 액세스 토큰 및 리프레시 토큰 생성
            JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
            JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

            saveUserToken(user, refresh.token(), refresh.expiry());

            // 사용자 정보 생성
            UserInfoDto userInfo = buildUserInfoDto(user);

            // 토큰 쌍과 사용자 정보 반환
            return new AuthResult(new TokenPair(access.token(), refresh.token()), userInfo);

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
        if (userRepository.existsByEmail(email)) {
            throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
        }
    }

    /**
     * User 엔티티로부터 UserInfoDto 생성
     *
     * @param user User 엔티티
     * @return UserInfoDto
     */
    private UserInfoDto buildUserInfoDto(User user) {
        return UserInfoDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isNewUser(false)  // 기존 회원으로 간주
                .build();
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
        user.setTermsAgreed(request.getIsTermsAgreed());
        user.setPrivacyPolicyAgreed(request.getIsPrivacyPolicyAgreed());
        user.setMarketingAgreed(request.getIsMarketingAgreed());
        user.setCreatedAt(LocalDateTime.now());

        try {
            userRepository.save(user); // save 시 자동으로 userId 세팅됨
        } catch (DataIntegrityViolationException e) {
            String message = e.getRootCause() != null ? e.getRootCause().getMessage() : "";
            if (message.contains("phone_number")) {
                throw new RestApiException(UserErrorStatus.DUPLICATE_PHONE_NUMBER);
            } else if (message.contains("email")) {
                throw new RestApiException(UserErrorStatus.DUPLICATE_EMAIL);
            } else {
                throw new RestApiException(UserErrorStatus.DB_INSERT_FAILED);
            }
        }

        return user;

    }

    /**
     * UserCredential 저장
     * 주어진 User 엔티티에 대해 인증 정보를 생성하고 저장합니다.
     * 비밀번호는 암호화하여 저장되며, OAuthProvider 및 OAuth ID 설정이 가능합니다.
     *
     * @param user        대상 User 엔티티 (이미 저장된 상태여야 함)
     * @param rawPassword 평문 비밀번호 (암호화되어 저장됨), OAuth 로그인 사용자는 null
     * @param provider    로그인 제공자 정보 (LOCAL, KAKAO 등)
     * @param oauthId     현재는 OAuth 와 Local 모두 NULL, 추후 고려
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserCredential(User user, String rawPassword, OAuthProvider provider, String oauthId) {
        UserCredential credential = new UserCredential();
        credential.setUser(user); // userId 대신 User 객체 전체 설정

        if (rawPassword != null) {
            credential.setPasswordHash(passwordEncoder.encode(rawPassword));
        } else {
            credential.setPasswordHash(null);
        }

        credential.setOauthProvider(provider);
        credential.setOauthId(oauthId);

        user.setCredential(credential);

        try {
            userCredentialRepository.save(credential);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_CREDENTIAL_INSERT_FAILED);
        }
    }


    /**
     * UserProfile 저장
     * 키, 몸무게, 성별, 체형 ID 등 프로필 정보 저장
     *
     * @param user    저장 대상 User 객체 (DB에 이미 저장된 상태)
     * @param request 회원가입 요청 정보 (CommonSignupRequest 구현체)
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserProfile(User user, CommonSignupRequest request) {
        UserProfile profile = new UserProfile();
        profile.setUser(user); // 연관 관계 설정
        profile.setGender(request.getGender());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());

        BodyType bodyType = bodyTypeRepository.findById(request.getBodyTypeId())
                .orElseThrow(() -> new RestApiException(UserErrorStatus.INVALID_BODY_TYPE));

        profile.setBodyType(bodyType);

        user.setProfile(profile);

        try {
            userProfileRepository.save(profile);
        } catch (DataAccessException e) {
            throw new RestApiException(UserErrorStatus.DB_PROFILE_INSERT_FAILED);
        }
    }

    /**
     * Refresh Token 저장 (Upsert 방식)
     * 기존 토큰이 있으면 update, 없으면 insert
     * @param user 저장 대상 User 객체 (DB에 이미 저장된 상태)
     * @param refreshToken 발급된 리프레시 토큰 문자열
     * @param expiredAt 토큰 만료일시
     * @throws RestApiException DB 저장 중 오류 발생 시
     */
    private void saveUserToken(User user, String refreshToken, LocalDateTime expiredAt) {
        // 기존 토큰이 있으면 update, 없으면 insert
        UserToken userToken = userTokenRepository.findByUserId(user.getId())
            .orElse(null);
        if (userToken == null) {
            userToken = new UserToken();
            userToken.setUser(user);
        }
        userToken.setRefreshToken(refreshToken);
        userToken.setExpiredAt(expiredAt);
        try {
            userTokenRepository.save(userToken);
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
    public AuthResult login(LocalLoginRequestDto request) {
        try {
            // 인증 토큰 생성: 이메일, 비밀번호 정보 포함
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

            // authenticationManager를 통해 인증 시도 (UserDetailsService + PasswordEncoder가 검증 수행)
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 인증 성공 시 인증 객체에서 사용자 정보 획득
            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

            Integer userId = principalDetails.getUserId();
            String email = principalDetails.getEmail();

            // JWT 액세스 토큰 및 리프레시 토큰 생성
            JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
            JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

            // 발급된 리프레시 토큰 DB 저장
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
            saveUserToken(user, refresh.token(), refresh.expiry());

            // 사용자 정보 생성
            UserInfoDto userInfo = buildUserInfoDto(user);

            // 토큰 쌍과 사용자 정보 반환
            return new AuthResult(new TokenPair(access.token(), refresh.token()), userInfo);

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
     * 4) 신규회원이면 null 토큰과 사용자 정보를 반환, 기존 회원이면 JWT 발급하여 로그인 처리
     *
     * @param request authorizationCode 카카오 인가 코드
     * @return 인증 결과 (토큰 쌍과 사용자 정보)
     * @throws RestApiException 토큰 발급 실패, 이메일 누락, 회원 미존재 등 예외 상황
     */
    @Transactional
    public AuthResult processKakaoLogin(KakaoLoginRequestDto request) {
        // 카카오 토큰 발급
        Map<String, Object> tokenInfo = requestTokenWithKakao(request.getAuthorizationCode());
        String accessToken = (String) tokenInfo.get("access_token");

        // 카카오 사용자 정보 조회
        Map<String, Object> kakaoUserInfo = requestUserInfoWithKakao(accessToken);
        Object kakaoAccountObj = kakaoUserInfo.get("kakao_account");
        if (!(kakaoAccountObj instanceof Map)) {
            throw new RestApiException(UserErrorStatus.KAKAO_USERINFO_REQUEST_FAILED);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
        String email = (String) kakaoAccount.get("email");
        Object profileObj = kakaoAccount.get("profile");
        if (!(profileObj instanceof Map)) {
            throw new RestApiException(UserErrorStatus.KAKAO_USERINFO_REQUEST_FAILED);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) profileObj;
        String nickname = (String) profile.get("nickname");

        // 회원 존재 여부 확인
        if (!userRepository.existsByEmail(email)) {
            // 신규 회원: null 토큰과 기본 사용자 정보 반환 (추가 정보 입력 필요)
            UserInfoDto userInfo = UserInfoDto.builder()
                    .name(nickname)
                    .email(email)
                    .role(UserRole.USER)
                    .isNewUser(true)
                    .build();

            return new AuthResult(new TokenPair(null, null), userInfo);
        }

        User user = userRepository.findByEmailWithProfile(email)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));

        Integer userId = user.getId();

        PrincipalDetails principalDetails = new PrincipalDetails(user, "");
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 발급
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, email);
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, email);

        // 발급된 리프레시 토큰 DB 저장
        saveUserToken(user, refresh.token(), refresh.expiry());

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(user.getName())
                .email(email)  // 카카오에서 받은 이메일 사용
                .role(user.getRole())
                .isNewUser(false)
                .build();

        return new AuthResult(new TokenPair(access.token(), refresh.token()), userInfo);
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
    public AuthResult completeSignupWithKakao(KakaoSignupRequestDto request) {
        // 이메일 중복 체크 및 회원 생성, 저장
        validateDuplicateEmail(request.getEmail());
        User user = createAndSaveUser(request);
        saveUserCredential(user, null, OAuthProvider.KAKAO, null);
        saveUserProfile(user, request);

        // PrincipalDetails 생성 (password는 null or "")
        PrincipalDetails principalDetails = new PrincipalDetails(user, "");

        // 인증 토큰 생성 (인증매니저가 처리할 authentication)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

        // 인증 성공 시 SecurityContext에 저장 (로그인 상태 유지)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT 토큰 생성 및 저장
        Integer userId = user.getId();
        JwtProvider.JwtWithExpiry access = jwtProvider.generateAccessToken(userId, user.getEmail());
        JwtProvider.JwtWithExpiry refresh = jwtProvider.generateRefreshToken(userId, user.getEmail());
        saveUserToken(user, refresh.token(), refresh.expiry());

        // 사용자 정보 생성
        UserInfoDto userInfo = buildUserInfoDto(user);

        // 토큰 쌍과 사용자 정보 반환
        return new AuthResult(new TokenPair(access.token(), refresh.token()), userInfo);
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 재발급하는 메서드
     *
     * @param request HttpServletRequest - 쿠키에서 리프레시 토큰을 추출
     * @return AuthResponseDto 사용자 정보가 포함된 응답 DTO (토큰은 쿠키로 설정됨)
     * @throws RestApiException 토큰 유효성 실패 혹은 사용자 정보 미발견 시 예외 발생
     */
    @Transactional
    public AuthResult refreshAccessToken(HttpServletRequest request) {
        // 쿠키에서 리프레시 토큰 추출
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new RestApiException(UserErrorStatus.INVALID_TOKEN);
        }

        // 리프레시 토큰 유효성 검사
        if (!jwtProvider.validateToken(refreshToken)) {
            if (jwtProvider.isTokenExpired(refreshToken)) {
                throw new RestApiException(UserErrorStatus.EXPIRED_REFRESH_TOKEN);
            }
            throw new RestApiException(UserErrorStatus.INVALID_TOKEN);
        }

        // 리프레시 토큰에서 사용자 정보 추출
        String email = jwtProvider.getEmailFromToken(refreshToken);
        Integer userId = jwtProvider.getUserIdFromToken(refreshToken);

        // DB에서 리프레시 토큰 확인
        UserToken savedToken = userTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.INVALID_TOKEN));

        // 새 토큰 발급
        JwtProvider.JwtWithExpiry newAccess = jwtProvider.generateAccessToken(userId, email);
        JwtProvider.JwtWithExpiry newRefresh = jwtProvider.generateRefreshToken(userId, email);

        // DB에 리프레시 토큰 업데이트
        savedToken.updateToken(newRefresh.token(), newRefresh.expiry());
        userTokenRepository.save(savedToken);

        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND));
        UserInfoDto userInfo = buildUserInfoDto(user);

        // 토큰 쌍과 사용자 정보 반환
        return new AuthResult(new TokenPair(newAccess.token(), newRefresh.token()), userInfo);
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
    @Transactional
    public UserIdResponseDto logout(PrincipalDetails principal) {
        Integer userId = principal.getUserId();
        try {
            int deletedCount = userTokenRepository.deleteByUserId(userId);
            if (deletedCount == 0) {
                throw new RestApiException(UserErrorStatus.LOGOUT_NO_ACTIVE_SESSION);
            }
            return new UserIdResponseDto(userId);
        } catch (Exception e) {
            throw new RestApiException(UserErrorStatus.LOGOUT_FAILED);
        }
    }

}
