package com.yourmode.yourmodebackend.domain.user.service;

import com.yourmode.yourmodebackend.domain.user.dto.request.PasswordChangeRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsSendRequestDto;
import com.yourmode.yourmodebackend.domain.user.dto.request.SmsVerifyRequestDto;

public interface PasswordResetService {
    void sendSMS(SmsSendRequestDto request);
    void verifyCode(SmsVerifyRequestDto request);
    void changePassword(PasswordChangeRequestDto request);

}
