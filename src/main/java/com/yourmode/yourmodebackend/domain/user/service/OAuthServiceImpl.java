package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.KakaoSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.TokenPairDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.enums.UserRole;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import com.yourmode.yourmodebackend.global.config.security.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuthServiceImpl implements OAuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final UserManagementService userManagementService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드를 이용해 카카오에서 액세스 토큰과 리프레시 토큰을 요청하는 메서드
     *
     * @param authorizationCode 카카오에서 받은 인가 코드
     * @return 토큰 정보가 담긴 Map (access_token, refresh_token 등)
     * @throws RestApiException 카카오 API 호출 실패  시 발생
     */
    @Override
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
    @Override
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
     * 카카오 콜백 처리를 위한 메서드
     * 신규 사용자인 경우 토큰을 null로 설정하고, 기존 사용자인 경우 토큰을 설정합니다.
     *
     * @param code 카카오 인가 코드
     * @return JWT 토큰과 유저 정보가 포함된 응답 DTO (신규 사용자는 토큰이 null)
     * @throws RestApiException 카카오 API 호출 실패 시 예외 발생
     */
    @Override
    @Transactional
    public AuthResultDto handleKakaoCallback(String code) {
        // 카카오 토큰 발급
        Map<String, Object> tokenInfo = requestTokenWithKakao(code);
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

        // 기존회원인 경우, 토큰과 사용자 정보 반환 특정 url로 반환
        if (!userRepository.existsByEmail(email)) {
            // 신규회원은 회원가입 완료 페이지로 리다이렉트
            UserInfoDto userInfo = UserInfoDto.builder()
                    .name(nickname)
                    .email(email)
                    .role(UserRole.USER)
                    .isNewUser(true)
                    .build();

            return new AuthResultDto(new TokenPairDto(null, null), userInfo);
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
        userManagementService.saveUserToken(user, refresh.token(), refresh.expiry());

        UserInfoDto userInfo = UserInfoDto.builder()
                .name(user.getName())
                .email(email)  // 카카오에서 받은 이메일 사용
                .role(user.getRole())
                .isNewUser(false)
                .build();

        // 기존회원은 메인 페이지 또는 대시보드로 리다이렉트
        return new AuthResultDto(new TokenPairDto(access.token(), refresh.token()), userInfo);
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
     * @return JWT 토큰과 유저 정보가 포함된 응답 DTO
     */
    @Override
    @Transactional
    public AuthResultDto completeSignupWithKakao(KakaoSignupRequestDto request) {
        // 이메일 중복 체크 및 회원 생성, 저장
        userManagementService.validateDuplicateEmail(request.getEmail());
        User user = userManagementService.createAndSaveUser(request);
        userManagementService.saveUserCredential(user, null, OAuthProvider.KAKAO, null);
        userManagementService.saveUserProfile(user, request);

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
        userManagementService.saveUserToken(user, refresh.token(), refresh.expiry());

        // 사용자 정보 생성
        UserInfoDto userInfo = userManagementService.buildUserInfoDto(user);

        // 토큰 쌍과 사용자 정보 반환
        return new AuthResultDto(new TokenPairDto(access.token(), refresh.token()), userInfo);
    }

}