package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.entity.*;
import com.yourmode.yourmodebackend.domain.user.dto.request.PasswordChangeRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsSendRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsVerifyRequestDto;
import com.yourmode.yourmodebackend.domain.user.redis.SmsRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserCredentialRepository;
import com.yourmode.yourmodebackend.domain.user.repository.UserRepository;
import com.yourmode.yourmodebackend.domain.user.status.UserErrorStatus;
import com.yourmode.yourmodebackend.domain.user.util.SmsCertificationUtil;
import com.yourmode.yourmodebackend.global.common.exception.RestApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
    private final SmsCertificationUtil smsCertificationUtil; // SMS 인증 유틸리티 객체
    private final SmsRepository smsRepository; // SMS 레포지토리 객체 (Redis)
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_SMS_SEND_COUNT = 5; // 1시간에 최대 5회 제한
    private static final int COUNT_LIMIT_SECONDS = 3600; // 1시간(3600초)

    /**
     * SMS 인증 코드를 발송합니다.
     * 1) 전화번호로 가입된 사용자 존재 여부 확인
     * 2) 1시간 내 발송 횟수 제한 체크 (최대 5회)
     * 3) 6자리 랜덤 인증 코드 생성
     * 4) SMS 발송 및 Redis에 인증 코드 저장
     *
     * @param request SMS 발송 요청 DTO (전화번호 포함)
     * @throws RestApiException
     *         - 해당 전화번호로 가입된 사용자가 없는 경우 USER_NOT_FOUND_PHONE_NUMBER 상태로 예외 발생
     *         - 1시간 내 발송 횟수가 5회를 초과한 경우 SMS_SEND_LIMIT_EXCEEDED 상태로 예외 발생
     *         - SMS 발송 실패 시 SMS_SEND_FAILED 상태로 예외 발생
     */
    @Transactional
    public void sendSMS(SmsSendRequestDto request) {
        String phoneNum = request.getPhoneNumber();
        // 해당 전화번호로 가입된 사용자가 있는지 확인
        User user = userRepository.findByPhoneNumber(phoneNum)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND_PHONE_NUMBER));

        // 발송 횟수 체크
        long sendCount = smsRepository.incrementSendCount(phoneNum, COUNT_LIMIT_SECONDS);
        if (sendCount > MAX_SMS_SEND_COUNT) {
            throw new RestApiException(UserErrorStatus.SMS_SEND_LIMIT_EXCEEDED);
        }

        String certificationCode = Integer.toString((int)(Math.random() * (999999 - 100000 + 1)) + 100000);

        try {
            smsCertificationUtil.sendSMS(phoneNum, certificationCode);
            smsRepository.createSmsCertification(phoneNum, certificationCode);
        } catch (Exception e) {
            throw new RestApiException(UserErrorStatus.SMS_SEND_FAILED);
        }
    }

    /**
     * SMS 인증 코드를 검증합니다.
     * 1) 전화번호와 인증 코드의 일치 여부 확인
     * 2) 인증 성공 시 Redis에서 인증 코드 삭제
     * 3) 인증 완료 플래그를 Redis에 5분간 저장
     *
     * @param request SMS 인증 코드 검증 요청 DTO (전화번호, 인증 코드 포함)
     * @throws RestApiException
     *         - 인증 코드가 일치하지 않는 경우 INVALID_VERIFICATION_CODE 상태로 예외 발생
     */
    @Transactional
    public void verifyCode(SmsVerifyRequestDto request) {
        boolean verified = isVerify(request.getPhoneNumber(), request.getVerificationCode());
        if (verified) {
            smsRepository.deleteSmsCertification(request.getPhoneNumber()); // 인증 코드 삭제
            smsRepository.setVerificationPassed(request.getPhoneNumber(), 5 * 60); // 5분간 인증 완료 플래그 저장 (예: 5분)
        } else {
            throw new RestApiException(UserErrorStatus.INVALID_VERIFICATION_CODE);
        }
    }

    /**
     * 전화번호와 인증 코드의 일치 여부를 검증합니다.
     * 1) Redis에 해당 전화번호의 인증 코드 존재 여부 확인
     * 2) 저장된 인증 코드와 입력된 인증 코드 일치 여부 확인
     *
     * @param phoneNum 검증할 전화번호
     * @param certificationCode 검증할 인증 코드
     * @return 인증 코드 일치 여부 (true: 일치, false: 불일치)
     */
    public boolean isVerify(String phoneNum, String certificationCode) {
        return smsRepository.hasCertificationCode(phoneNum) && // 전화번호에 대한 키가 존재하고
                smsRepository.getSmsCertification(phoneNum).equals(certificationCode); // 저장된 인증 코드와 입력된 인증 코드가 일치하는지 확인
    }

    /**
     * SMS 인증 완료 후 비밀번호를 변경합니다.
     * 1) SMS 인증 완료 여부 확인 (Redis에서 인증 완료 플래그 체크)
     * 2) 전화번호로 사용자 조회
     * 3) 사용자의 인증 정보 조회
     * 4) 새 비밀번호를 암호화하여 저장
     * 5) 인증 완료 플래그 삭제 (재사용 방지)
     *
     * @param request 비밀번호 변경 요청 DTO (전화번호, 새 비밀번호 포함)
     * @throws RestApiException
     *         - SMS 인증이 완료되지 않은 경우 UNAUTHORIZED_PASSWORD_CHANGE 상태로 예외 발생
     *         - 해당 전화번호로 가입된 사용자가 없는 경우 USER_NOT_FOUND_PHONE_NUMBER 상태로 예외 발생
     *         - 사용자의 인증 정보를 찾을 수 없는 경우 CREDENTIAL_NOT_FOUND 상태로 예외 발생
     */
    @Transactional
    public void changePassword(PasswordChangeRequestDto request) {
        String phoneNum = request.getPhoneNumber();

        // 인증 완료 여부 확인
        if (!smsRepository.hasVerificationPassed(phoneNum)) {
            throw new RestApiException(UserErrorStatus.UNAUTHORIZED_PASSWORD_CHANGE);
        }

        User user = userRepository.findByPhoneNumber(phoneNum)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.USER_NOT_FOUND_PHONE_NUMBER));

        UserCredential credential = userCredentialRepository.findByUser(user)
                .orElseThrow(() -> new RestApiException(UserErrorStatus.CREDENTIAL_NOT_FOUND));

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userCredentialRepository.save(credential);

        // 변경 후 인증 완료 플래그 삭제 (재사용 방지)
        smsRepository.deleteVerificationPassed(phoneNum);
    }
}
