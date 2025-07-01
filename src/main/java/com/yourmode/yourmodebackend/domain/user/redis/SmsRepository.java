package com.yourmode.yourmodebackend.domain.user.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.time.Duration;

@RequiredArgsConstructor
@Repository
public class SmsRepository {
    private final String PREFIX = "sms:";
    private final String CODE_SUFFIX = ":code";
    private final String VERIFIED_SUFFIX = ":verified";
    private final String COUNT_SUFFIX = ":count";  // 횟수 카운트 키

    private final StringRedisTemplate stringRedisTemplate;

    // 인증 코드 저장
    public void createSmsCertification(String phone, String code){
        int LIMIT_TIME = 3 * 60; // 3분
        stringRedisTemplate.opsForValue()
                .set(PREFIX + phone + CODE_SUFFIX, code, Duration.ofSeconds(LIMIT_TIME));
    }

    // 인증 코드 조회
    public String getSmsCertification(String phone){
        return stringRedisTemplate.opsForValue().get(PREFIX + phone + CODE_SUFFIX);
    }

    // 인증 코드 삭제
    public void deleteSmsCertification(String phone){
        stringRedisTemplate.delete(PREFIX + phone + CODE_SUFFIX);
    }

    // 인증 코드 존재 여부 확인
    public boolean hasCertificationCode(String phone){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + phone + CODE_SUFFIX));
    }

    // 인증 완료 플래그 저장
    public void setVerificationPassed(String phone, int ttlSeconds){
        stringRedisTemplate.opsForValue()
                .set(PREFIX + phone + VERIFIED_SUFFIX, "true", Duration.ofSeconds(ttlSeconds));
    }

    // 인증 완료 플래그 확인
    public boolean hasVerificationPassed(String phone){
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PREFIX + phone + VERIFIED_SUFFIX));
    }

    // 인증 완료 플래그 삭제
    public void deleteVerificationPassed(String phone){
        stringRedisTemplate.delete(PREFIX + phone + VERIFIED_SUFFIX);
    }

    // 발송 횟수 증가 및 TTL 설정 (예: 1시간 단위 제한)
    public long incrementSendCount(String phone, int limitSeconds) {
        String key = PREFIX + phone + COUNT_SUFFIX;
        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // 최초 증가 시 TTL 설정
            stringRedisTemplate.expire(key, Duration.ofSeconds(limitSeconds));
        }
        return count != null ? count : 0;
    }

    // 발송 횟수 조회
    public long getSendCount(String phone) {
        String key = PREFIX + phone + COUNT_SUFFIX;
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return 0;
        }
        try {
            return Long.parseLong(countStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // 발송 횟수 초기화(필요시)
    public void resetSendCount(String phone){
        stringRedisTemplate.delete(PREFIX + phone + COUNT_SUFFIX);
    }
}
