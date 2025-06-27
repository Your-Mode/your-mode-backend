package com.yourmode.yourmodebackend.domain.user.dto;

import com.yourmode.yourmodebackend.domain.user.enums.Gender;

public interface CommonSignupRequest {
    String getEmail();
    String getName();
    String getPhoneNumber();
    Boolean getIsTermsAgreed();
    Boolean getIsPrivacyPolicyAgreed();
    Boolean getIsMarketingAgreed();
    Float getHeight();
    Float getWeight();
    Gender getGender();
    Long getBodyTypeId();
}