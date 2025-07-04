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

    // 전화번호와 인증 코드를 검증하는 메서드
    public boolean isVerify(String phoneNum, String certificationCode) {
        return smsRepository.hasCertificationCode(phoneNum) && // 전화번호에 대한 키가 존재하고
                smsRepository.getSmsCertification(phoneNum).equals(certificationCode); // 저장된 인증 코드와 입력된 인증 코드가 일치하는지 확인
    }

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
