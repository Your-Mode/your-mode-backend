package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.entity.User;
import com.yourmode.yourmodebackend.domain.user.entity.UserToken;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalLoginRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.LocalSignupRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.AuthResultDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.TokenPairDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserIdResponseDto;
import com.yourmode.yourmodebackend.domain.user.dto.response.UserInfoDto;
import com.yourmode.yourmodebackend.domain.user.enums.OAuthProvider;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserTokenRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.global.config.security.jwt.JwtProvider;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import com.yourmode.yourmodebackend.global.config.security.auth.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final UserManagementService userManagementService;

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
    public AuthResultDto signUp(LocalSignupRequestDto request) {
        // 이메일 중복 체크 후 중복되면 예외 발생
        userManagementService.validateDuplicateEmail(request.getEmail());

        // User 생성 및 저장, DB에서 자동 생성된 PK(userId) 필드 값이 세팅됨
        User user = userManagementService.createAndSaveUser(request);

        // UserCredential 저장: 비밀번호 해시값 + OAuthProvider 정보
        userManagementService.saveUserCredential(user, request.getPassword(), OAuthProvider.LOCAL, null);

        // UserProfile 저장: 키, 몸무게, 성별, 체형 등 프로필 정보
                    userManagementService.saveUserProfile(user, request);

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

                                userManagementService.saveUserToken(user, refresh.token(), refresh.expiry());

                // 사용자 정보 생성
                UserInfoDto userInfo = userManagementService.buildUserInfoDto(user);

            // 토큰 쌍과 사용자 정보 반환
            return new AuthResultDto(new TokenPairDto(access.token(), refresh.token()), userInfo);

        } catch (AuthenticationException ex) {
            throw new RestApiException(UserErrorStatus.AUTHENTICATION_FAILED);
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
    public AuthResultDto login(LocalLoginRequestDto request) {
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
            userManagementService.saveUserToken(user, refresh.token(), refresh.expiry());

            // 사용자 정보 생성
            UserInfoDto userInfo = userManagementService.buildUserInfoDto(user);

            // 토큰 쌍과 사용자 정보 반환
            return new AuthResultDto(new TokenPairDto(access.token(), refresh.token()), userInfo);

        } catch (BadCredentialsException e) {
            throw new RestApiException(UserErrorStatus.INVALID_CREDENTIALS);
        } catch (AuthenticationException e) {
            throw new RestApiException(UserErrorStatus.AUTHENTICATION_FAILED);
        }
    }

    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 재발급하는 메서드
     *
     * @param request HttpServletRequest - 쿠키에서 리프레시 토큰을 추출
     * @return JWT 토큰과 유저 정보가 포함된 응답 DTO
     * @throws RestApiException 토큰 유효성 실패 혹은 사용자 정보 미발견 시 예외 발생
     */
    @Transactional
    public AuthResultDto refreshAccessToken(HttpServletRequest request) {
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
        UserInfoDto userInfo = userManagementService.buildUserInfoDto(user);

        // 토큰 쌍과 사용자 정보 반환
        return new AuthResultDto(new TokenPairDto(newAccess.token(), newRefresh.token()), userInfo);
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
